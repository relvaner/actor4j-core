package actor4j.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import actor4j.core.Actor;
import actor4j.core.messages.ActorMessage;
import tools4j.utils.Range;

public final class CommPattern {
	protected static Range loadBalancing(int rank, int size, int arr_size) {
		int quotient  = arr_size/size;
		int remainder = arr_size%size;
		
		int low  = 0;
		int high = 0;
	
		if (rank<remainder) {
			low  = rank * quotient + rank;
			high = low + quotient;
		}
		else {
			low  = rank * quotient + remainder ;
			high = low + quotient - 1;
		}
		
		return new Range(low, high);
	}
	
	public static void broadcast(ActorMessage<?> message, Actor actor, ActorGroup group) {
		for (UUID dest : group)
			actor.send(message, dest);
	}
	
	public static <T> void scatter(List<T> list, int tag, Actor actor, ActorGroup group) {
		int i=0;
		for (UUID dest : group) {
			Range range = loadBalancing(i, group.size(), list.size());
			actor.send(new ActorMessage<>(new ArrayList<T>(list.subList(range.low, range.high+1)), tag, actor.getSelf(), dest));	
			i++;
		}
	}
}
