/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.core.protocols;

public final class ActorProtocolTag {
	public static final int INTERNAL_RESTART             = -1;
	public static final int INTERNAL_STOP                = -2;
	public static final int INTERNAL_STOP_SUCCESS        = -3;
	public static final int INTERNAL_KILL                = -4;
	public static final int INTERNAL_RECOVER             = -5;
	
	public static final int INTERNAL_PERSISTENCE_RECOVER = -6;
	public static final int INTERNAL_PERSISTENCE_SUCCESS = -7;
	public static final int INTERNAL_PERSISTENCE_FAILURE = -8;
	
	public static final int INTERNAL_ACTIVATE            = -9;
	public static final int INTERNAL_DEACTIVATE          = -10;
}
