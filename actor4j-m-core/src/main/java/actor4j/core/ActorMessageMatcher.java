/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import actor4j.function.Consumer;
import actor4j.function.Predicate;

public class ActorMessageMatcher {
	protected class MatchTuple {
		public Predicate<ActorMessage<?>> predicate;
		public Consumer<ActorMessage<?>> action;
	}
	
	protected List<MatchTuple> matches;
	protected List<MatchTuple> matchesAny;
	
	public ActorMessageMatcher() {
		matches    = new LinkedList<>();
		matchesAny = new LinkedList<>();
	}
		
	public ActorMessageMatcher match(UUID source, Consumer<ActorMessage<?>> action) {
		checkAction(action);
		
		MatchTuple tuple = new MatchTuple();
		tuple.predicate = new Predicate<ActorMessage<?>>(){
			@Override
			public boolean test(ActorMessage<?> message) {
				return message.source.equals(source);
			}
		};
		tuple.action = action;
		matches.add(tuple);
		
		return this;
	}
	
	public ActorMessageMatcher match(int tag, Consumer<ActorMessage<?>> action) {
		checkAction(action);
		
		MatchTuple tuple = new MatchTuple();
		tuple.predicate = new Predicate<ActorMessage<?>>(){
			@Override
			public boolean test(ActorMessage<?> message) {
				return message.tag==tag;
			}
		};
		tuple.action = action;
		matches.add(tuple);
		
		return this;
	}
	
	public ActorMessageMatcher match(UUID source, int tag, Consumer<ActorMessage<?>> action) {
		checkAction(action);
		
		MatchTuple tuple = new MatchTuple();
		tuple.predicate = new Predicate<ActorMessage<?>>(){
			@Override
			public boolean test(ActorMessage<?> message) {
				return message.source.equals(source) && message.tag==tag;
			}
		};
		tuple.action = action;
		matches.add(tuple);
		
		return this;
	}
	
	public ActorMessageMatcher match(Predicate<ActorMessage<?>> predicate, Consumer<ActorMessage<?>> action) {
		checkPredicate(predicate);
		checkAction(action);
		
		MatchTuple tuple = new MatchTuple();
		tuple.predicate = predicate;
		tuple.action = action;
		matches.add(tuple);
		
		return this;
	}
	
	public ActorMessageMatcher matchAny(Consumer<ActorMessage<?>> action) {
		checkAction(action);
		
		MatchTuple tuple = new MatchTuple();
		tuple.predicate = new Predicate<ActorMessage<?>>(){
			@Override
			public boolean test(ActorMessage<?> message) {
				return true;
			}
		};
		tuple.action = action;
		matchesAny.add(tuple);
		
		return this;
	}
	
	public <T> void apply(ActorMessage<T> message) {
		for (MatchTuple tuple : matches)
			if (tuple.predicate.test(message)) {
				tuple.action.accept(message);
				break;
			}
		for (MatchTuple tuple : matchesAny)
			if (tuple.predicate.test(message))
				tuple.action.accept(message);
	}
	
	protected void checkPredicate(Predicate<ActorMessage<?>> predicate) {
		if (predicate==null)
			throw new NullPointerException("predicate is null");
	}
	
	protected void checkAction(Consumer<ActorMessage<?>> action) {
		if (action==null)
			throw new NullPointerException("action is null");
	}
}
