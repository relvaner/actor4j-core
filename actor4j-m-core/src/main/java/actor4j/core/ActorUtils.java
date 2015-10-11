/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core;

import java.util.UUID;

public final class ActorUtils {
	public static final UUID UUID_ZERO = UUID.fromString("00000000-0000-0000-0000-000000000000");
	
	public static String actorLabel(Actor actor) {
		return actor.getName()!=null ? actor.getName() : actor.getId().toString();
	}
}
