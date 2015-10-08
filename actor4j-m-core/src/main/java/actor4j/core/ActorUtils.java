package actor4j.core;

public final class ActorUtils {
	public static String actorLabel(Actor actor) {
		return actor.getName()!=null ? actor.getName() : actor.getId().toString();
	}
}
