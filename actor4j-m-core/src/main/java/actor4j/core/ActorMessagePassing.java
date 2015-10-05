package actor4j.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ActorMessagePassing {
	protected ActorSystem system;
	
	protected Map<UUID, Long> actorsMap; // ActorID -> ThreadID
	protected Map<Long, ActorThread> threadsMap;
	
	protected Map<UUID, Long> groupsMap; // GroupID -> ThreadID
	
	protected final UUID UUID_ALIAS = UUID.fromString("00000000-0000-0000-0000-000000000000");
	
	public ActorMessagePassing(ActorSystem system) {
		super();
		
		this.system = system;
		
		actorsMap = new ConcurrentHashMap<>();
		threadsMap = new HashMap<>();
		
		groupsMap = new ConcurrentHashMap<>();
	}
	
	public void post(ActorMessage<?> message) {
		post(message, null);
	}
	
	public void post(ActorMessage<?> message, String alias) {
		if (message==null)
			throw new NullPointerException();
		
		if (system.analyzeMode.get())
			system.analyzerThread.outerQueueL2.offer(message.clone());
		
		if (alias!=null) {
			UUID dest = system.aliases.get(alias);
			message.dest = (dest!=null) ? dest : UUID_ALIAS;
		}
		
		if (system.serverMode && !system.actors.containsKey(message.dest)) {
			system.executerService.client(message.clone(), alias);
			return;
		}
			
		if (system.parallelismMin==1 && system.parallelismFactor==1)
			((ActorThread)Thread.currentThread()).innerQueue.offer(message.clone());
		else {
			Long id_source = actorsMap.get(message.source);
			Long id_dest   = actorsMap.get(message.dest);
		
			if (id_dest!=null) {
				if (id_source!=null && id_source==id_dest && Thread.currentThread().getId()==id_source)
					threadsMap.get(id_dest).innerQueue.offer(message.clone());
				else
					threadsMap.get(id_dest).outerQueueL2.offer(message.clone());
			}
		}
	}
	
	public void postOuter(ActorMessage<?> message) {
		if (message==null)
			throw new NullPointerException();
		
		if (system.analyzeMode.get())
			system.analyzerThread.outerQueueL2.offer(message.clone());
		
		Long id_dest = actorsMap.get(message.dest);
		if (id_dest!=null)
			threadsMap.get(id_dest).outerQueueL2.offer(message.clone());
	}
	
	public void postServer(ActorMessage<?> message) {
		if (message==null)
			throw new NullPointerException();
		
		if (system.analyzeMode.get())
			system.analyzerThread.outerQueueL2.offer(message.clone());
		
		Long id_dest = actorsMap.get(message.dest);
		if (id_dest!=null)
			threadsMap.get(id_dest).serverQueueL2.offer(message.clone());
	}
	
	public void beforeRun(List<ActorThread> actorThreads) {
		system.actorBalancingOnCreation.balance(actorsMap, actorThreads, groupsMap);
		
		for(ActorThread t : actorThreads)
			threadsMap.put(t.getId(), t);
	}
	
	public void registerActor(Actor actor) {
		system.actorBalancingOnRuntime.registerActor(actorsMap, threadsMap, groupsMap, actor);
	}
	
	public void unregisterActor(Actor actor) {
		system.actorBalancingOnRuntime.unregisterActor(actorsMap, threadsMap, groupsMap, actor);
	}
}
