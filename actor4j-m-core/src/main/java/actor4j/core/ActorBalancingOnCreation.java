package actor4j.core;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ActorBalancingOnCreation {
	protected ActorSystem system;
	
	public ActorBalancingOnCreation(ActorSystem system) {
		super();
		
		this.system = system;
	}
	
	public void balance(Map<UUID, Long> actorsMap, List<ActorThread> actorThreads, Map<UUID, Long> groupsMap) {
		List<UUID> buffer = new LinkedList<>();
		for (UUID id : system.actors.keySet())
			buffer.add(id);
		
		int i=0;
		for (Actor actor : system.actors.values()) {
			if (actor instanceof ActorGroupMember) {
				Long id = groupsMap.get(((ActorGroupMember)actor).getGroupId());
				if (id==null) {
					id = actorThreads.get(i).getId();
					groupsMap.put(((ActorGroupMember)actor).getGroupId(), id);
					i++;
					if (i==actorThreads.size())
						i = 0;
				}
				if (buffer.remove(actor.getId()))
					actorsMap.put(actor.getId(), id);
			}
		}	
						
		i=0;
		for (UUID id : buffer) {
			actorsMap.put(id, actorThreads.get(i).getId());
			i++;
			if (i==actorThreads.size())
				i = 0;
		}
			
		/*
		int i=0;
		for (UUID id : system.actors.keySet()) {
			actorsMap.put(id, actorThreads.get(i).getId());
			i++;
			if (i==actorThreads.size())
				i = 0;
		}
		*/
	}
}
