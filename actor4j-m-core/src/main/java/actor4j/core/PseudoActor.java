package actor4j.core;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import org.jctools.queues.MpscArrayQueue;

import actor4j.core.actors.ActorWithRxStash;
import actor4j.core.exceptions.ActorInitializationException;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorFactory;
import rx.Observable;
import safety4j.SafetyManager;

public abstract class PseudoActor extends ActorWithRxStash {
	protected Queue<ActorMessage<?>> outerQueueL2;
	protected Queue<ActorMessage<?>> outerQueueL1;
	
	protected Observable<ActorMessage<?>> rxOuterQueueL1;
	
	public PseudoActor(ActorSystem system) {
		this(null, system);
	}
	
	public PseudoActor(String name, ActorSystem system) {
		super(name);
		this.system = system;
		
		outerQueueL2 = new MpscArrayQueue<>(50000);
		outerQueueL1 = new LinkedList<>();
		
		rxOuterQueueL1 = ActorMessageObservable.getMessages(outerQueueL1);
				
		system.system_addActor(this);
		/* preStart */
		preStart();
	}

	protected void safetyMethod(ActorMessage<?> message) {
		try {
			internal_receive(message);
		}
		catch(Exception e) {
			SafetyManager.getInstance().notifyErrorHandler(e, "pseudo", getId());
			system.actorStrategyOnFailure.handle(this, e);
		}	
	}
	
	protected boolean poll(Queue<ActorMessage<?>> queue) {
		boolean result = false;
		
		ActorMessage<?> message = queue.poll();
		if (message!=null) {
			safetyMethod(message);
			result = true;
		} 
		
		return result;
	}
	
	public void run() {
		boolean hasNextOuter = outerQueueL1.peek()!=null;
		if (!hasNextOuter && outerQueueL2.peek()!=null) {
			ActorMessage<?> message = null;
			for (int j=0; (message=outerQueueL2.poll())!=null && j<10000; j++)
				outerQueueL1.offer(message);
		}
		while (poll(outerQueueL1));
	}
	
	public Observable<ActorMessage<?>> runWithRx() {
		boolean hasNextOuter = outerQueueL1.peek()!=null;
		if (!hasNextOuter && outerQueueL2.peek()!=null) {
			ActorMessage<?> message = null;
			for (int j=0; (message=outerQueueL2.poll())!=null && j<10000; j++)
				outerQueueL1.offer(message);
		}
		
		return rxOuterQueueL1;
	}
	
	@Override
	public void send(ActorMessage<?> message) {
		system.send(message);
	}
	
	@Override
	public void send(ActorMessage<?> message, String alias) {
		system.send(message);
	}
	
	@Override
	protected UUID internal_addChild(Actor actor) {
		return null;
	}
	
	public UUID addChild(Class<? extends Actor> clazz, Object... args) throws ActorInitializationException {
		return null;
	}
	
	public UUID addChild(ActorFactory factory) {
		return null;
	}
}
