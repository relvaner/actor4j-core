/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.verification.example;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.verification.ActorVerification;
import actor4j.verification.ActorVerificationSM;
import actor4j.verification.ActorVerificationUtils;

public class Application {
	public static final int PING = 100;
	public static final int PONG = 101;
	
	protected static class Ping extends Actor implements ActorVerification {
		public Ping(String name) {
			super(name);
		}
		
		@Override
		public void receive(ActorMessage<?> message) {
			tell("ping", PING, message.source);
		}
		
		@Override
		public ActorVerificationSM verify() {
			ActorVerificationSM result = new ActorVerificationSM(this);
			result.addStateMarker("PING");
			result.addInTransition("PING", "PING", PONG);
			result.addOutTransition("PING", "PING", PING, "pong");
			
			return result;
		}
	}
	
	protected static class Pong extends Actor implements ActorVerification {
		@Override
		public void receive(ActorMessage<?> message) {
			tell("pong", PONG, message.source);
		}
		
		@Override
		public ActorVerificationSM verify() {
			ActorVerificationSM result = new ActorVerificationSM(this);
			result.addStateMarker("PONG");
			result.addInTransition("PONG", "PONG", PING);
			
			return result;
		}
	}

	public static void main(String[] args) {
		Ping ping = new Ping("ping");
		ActorVerificationSM sm = ping.verify();
		sm.addStateMarker("D");
		sm.addInTransition("PING", "D", 0);
		
		sm.addStateMarker("A");
		sm.addStateMarker("B");
		sm.addStateMarker("C");
		sm.addInTransition("A", "B", 0);
		sm.addInTransition("B", "C", 0);
		sm.addInTransition("C", "A", 0);
		
		System.out.println(ActorVerificationUtils.findCycles(sm.getGraph()));
		System.out.println(ActorVerificationUtils.findUnreachables(sm.getGraph(), "ping:PING"));
		
		System.out.println(ActorVerificationUtils.findDead(
				sm.getGraph(), ActorVerificationUtils.findUnreachables(sm.getGraph(), "ping:PING")));
		
		System.out.println(sm.getGraph().getAllEdges("ping:PING", "ping:PING"));
	}
}
