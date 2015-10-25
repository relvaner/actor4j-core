package actor4j.testing;

import java.util.UUID;

import actor4j.core.ActorCell;
import actor4j.core.ActorSystem;
import actor4j.core.actors.Actor;

public class TestSystem extends ActorSystem {
	public ActorCell underlyingCell(UUID id) {
		return cells.get(id);
	}
	
	public Actor underlyingActor(UUID id) {
		ActorCell cell = cells.get(id);
		return (cell!=null)? cell.getActor() : null;
	}
}
