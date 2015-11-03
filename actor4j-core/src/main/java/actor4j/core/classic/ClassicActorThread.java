/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core.classic;

import java.util.UUID;

import actor4j.core.ActorSystemImpl;
import actor4j.core.ActorThread;
import actor4j.core.messages.ActorMessage;

public class ClassicActorThread extends ActorThread {
	public ClassicActorThread(ActorSystemImpl system) {
		super(system);
	}

	@Override
	public void onRun() {
		boolean hasNextDirective = false;
		boolean hasNextServer 	 = false;
		boolean hasNextOuter     = false;
		boolean hasNextInner     = false;
		
		ClassicActorCell cell = null;
		while (!isInterrupted()) {
			for (UUID id : ((ClassicActorMessageDispatcher)system.getMessageDispatcher()).cellsOnThread.get(getId())) {
				cell = (ClassicActorCell)system.getCells().get(id);
				
				while (poll(cell.getDirectiveQueue())) 
					hasNextDirective=true;
				
				if (system.isClientMode()) {
					hasNextServer = poll(cell.getServerQueueL1());
					if (!hasNextServer && cell.getServerQueueL2().peek()!=null) {
						ActorMessage<?> message = null;
						for (int j=0; (message=cell.getServerQueueL2().poll())!=null && j<system.getBufferQueueSize(); j++)
							cell.getServerQueueL1().offer(message);
					
						for (int i=0; i<((ClassicActorSystemImpl)system).getThroughput() && poll(cell.getServerQueueL1()); i++)
							hasNextServer = true;
					}
				}
				
				hasNextOuter = poll(cell.getOuterQueueL1());
				if (!hasNextOuter && cell.getOuterQueueL2().peek()!=null) {
					ActorMessage<?> message = null;
					for (int j=0; (message=cell.getOuterQueueL2().poll())!=null && j<system.getBufferQueueSize(); j++)
						cell.getOuterQueueL1().offer(message);
					
					for (int i=0; i<((ClassicActorSystemImpl)system).getThroughput() && poll(cell.getOuterQueueL1()); i++)
						hasNextOuter = true;
				}
				
				for (int i=0; i<((ClassicActorSystemImpl)system).getThroughput() && poll(cell.getInnerQueue()); i++)
					hasNextInner = true;
				
				if (isInterrupted())
					break;
			}
			
			if ((!hasNextInner && !hasNextOuter && !hasNextServer && !hasNextDirective))
				if (!system.isSoftMode())
					yield();
				else {
					try {
						sleep(system.getSoftSleep());
					} catch (InterruptedException e) {
						interrupt();
					}
				}
		}
	}
}
