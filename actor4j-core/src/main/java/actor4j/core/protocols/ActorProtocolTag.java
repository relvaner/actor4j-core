/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core.protocols;

public final class ActorProtocolTag {
	public static final int INTERNAL_RESTART              = -1;
	public static final int INTERNAL_STOP                 = -2;
	public static final int INTERNAL_STOP_SUCCESS         = -3;
	public static final int INTERNAL_KILL                 = -4;
	public static final int INTERNAL_RECOVER              = -5;
	
	public static final int INTERNAL_PERSISTENCE_RECOVER  = -6;
	public static final int INTERNAL_PERSISTENCE_SUCCESS  = -7;
	public static final int INTERNAL_PERSISTENCE_FAILURE  = -8;
}
