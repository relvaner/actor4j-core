/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.core.exceptions;

public class ActorKilledException extends RuntimeException {
	protected static final long serialVersionUID = 5887686326593649655L;
	
	public ActorKilledException() {
		super("actor was killed");
	}
}
