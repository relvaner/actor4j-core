/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.verification.example;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import static actor4j.core.utils.ActorLogger.*;

import java.util.LinkedList;
import java.util.List;

import actor4j.verification.ActorVerification;
import actor4j.verification.ActorVerificationSM;
import actor4j.verification.ActorVerificationUtils;
import actor4j.verification.ActorVerificator;

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
			result
				.addInitialStateMarker("PING")
				.addInTransition("PING", "PING", PONG)
				.addOutTransition("PING", "PING", PING, "pong")
			
				.addStateMarker("D")
				.addInTransition("PING", "D", 0)
			
				.addStateMarker("A")
				.addStateMarker("B")
				.addStateMarker("C")
				.addInTransition("A", "B", 0)
				.addInTransition("B", "C", 0)
				.addInTransition("C", "A", 0);
			
			return result;
		}
	}
	
	protected static class Pong extends Actor implements ActorVerification {
		public Pong(String name) {
			super(name);
		}
		
		@Override
		public void receive(ActorMessage<?> message) {
			tell("pong", PONG, message.source);
		}
		
		@Override
		public ActorVerificationSM verify() {
			ActorVerificationSM result = new ActorVerificationSM(this);
			result
				.addInitialStateMarker("PONG")
				.addInTransition("PONG", "PONG", PING)
				.addOutTransition("PONG", "PONG", PONG, "ping");
			
			return result;
		}
	}

	public static void main(String[] args) {
		ActorVerificator verificator = new ActorVerificator();
		
		verificator.addActor(() -> new Ping("ping"));
		verificator.addActor(() -> new Pong("pong"));
		
		String globalIntialStateMarker = "ping:PING";
		
		List<ActorVerificationSM> list = new LinkedList<>();
		
		verificator.verifyAll((sm) -> {
			list.add(sm);
			logger().debug(String.format("%s - Cycles: %s", sm.getName(), ActorVerificationUtils.findCycles(sm.getGraph())));
			logger().debug(String.format("%s - Unreachables: %s", sm.getName(), ActorVerificationUtils.findUnreachables(sm.getGraph(), sm.getIntialStateMarker())));
			logger().debug(String.format("%s - Deads: %s", sm.getName(), ActorVerificationUtils.findDead(
					sm.getGraph(), ActorVerificationUtils.findUnreachables(sm.getGraph(), sm.getIntialStateMarker()))));
			
			logger().debug(String.format("%s - Edges (initial state, self reference): %s", sm.getName(), sm.getGraph().getAllEdges(sm.getIntialStateMarker(), sm.getIntialStateMarker())));
		}, (graph) -> {
			ActorVerificationUtils.interconnect(list, graph);
			logger().debug(String.format("All - Cycles: %s", ActorVerificationUtils.findCycles(graph)));
			logger().debug(String.format("All - Unreachables: %s", ActorVerificationUtils.findUnreachables(graph, globalIntialStateMarker)));
			logger().debug(String.format("All - Deads: %s", ActorVerificationUtils.findDead(
					graph, ActorVerificationUtils.findUnreachables(graph, globalIntialStateMarker))));
		});
	}
}
