package actor4j.testing;

import java.util.UUID;

import actor4j.core.Actor;
import actor4j.core.ActorSystem;

public class TestSystem extends ActorSystem {
	public Actor underlyingActor(UUID id) {
		return actors.get(id);
	}
}
