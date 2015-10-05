package actor4j.core;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class ActorBalancingOnCreationManual {
	protected ActorSystem system;
	
	public ActorBalancingOnCreationManual(ActorSystem system) {
		super();
		
		this.system = system;
	}
	
	public abstract void balance(Map<UUID, Long> actorsMap, List<ActorThread> actorThreads);
	
	public void balanceActor(Map<UUID, Long> actorsMap, Long threadId, Actor actor) {
		actorsMap.put(actor.getId(), threadId);
	}
	
	public void balanceActors(Map<UUID, Long> actorsMap, List<ActorThread> actorThreads, List<Actor> actors) {
		int i=0;
		for (Actor actor : actors) {
			balanceActor(actorsMap, actorThreads.get(i).getId(), actor);
			i++;
			if (i==actorThreads.size())
				i = 0;
		}
	}
	
	public void balanceGroup(Map<UUID, Long> actorsMap, Long threadId, ActorGroup group) {
		for (UUID id : group)
			actorsMap.put(id, threadId);
	}
	
	public void balanceGroups(Map<UUID, Long> actorsMap, List<ActorThread> actorThreads, List<ActorGroup> groups) {
		int i=0;
		for (ActorGroup group : groups) {
			balanceGroup(actorsMap, actorThreads.get(i).getId(), group);
			i++;
			if (i==actorThreads.size())
				i = 0;
		}
	}
}
