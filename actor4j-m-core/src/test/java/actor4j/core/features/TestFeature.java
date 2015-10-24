package actor4j.core.features;

import java.util.UUID;

import actor4j.core.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorFactory;
import actor4j.testing.TestSystem;

public class TestFeature {
	protected TestSystem system;

	public void before() {
		system = new TestSystem();
		system.setParallelismMin(1);
	}
	
	public void test() {
		UUID dest = system.addActor(new ActorFactory() { 
			@Override
			public Actor create() {
				return new Actor("TestFeatureActor") {
					@Override
					public void receive(ActorMessage<?> message) {
						/* empty */
					}
				};
			}
		});
		
		Actor actor = system.underlyingActor(dest);
	}
	
	public static void main(String[] args) {
		TestFeature testFeature = new TestFeature();
		testFeature.before();
		testFeature.test();
	}
}
