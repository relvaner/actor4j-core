package actor4j.core.features;

import java.util.UUID;

import actor4j.core.Actor;
import actor4j.core.ActorFactory;
import actor4j.core.ActorMessage;
import actor4j.core.ActorSystem;

public class SafetyFeature {
	protected ActorSystem system;

	public void before() {
		system = new ActorSystem();
		system.setParallelismMin(1);
	}
	
	public void test() {
		UUID dest = system.addActor(new ActorFactory() { 
			@Override
			public Actor create() {
				return new Actor("SafetyFeatureActor") {
					@Override
					protected void receive(ActorMessage<?> message) {
						throw new NullPointerException();
					}
				};
			}
		});
		
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.start();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdown(true);
	}
	
	public static void main(String[] args) {
		SafetyFeature safetyFeature = new SafetyFeature();
		safetyFeature.before();
		safetyFeature.test();
	}
}
