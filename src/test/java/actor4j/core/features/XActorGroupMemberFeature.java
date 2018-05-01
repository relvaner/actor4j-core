package actor4j.core.features;

import org.junit.Before;

import actor4j.core.ActorSystem;
import actor4j.core.XActorSystemImpl;

public class XActorGroupMemberFeature extends ActorGroupMemberFeature {
	@Before
	public void before() {
		system = new ActorSystem("x-actor4j", XActorSystemImpl.class);
	}
}
