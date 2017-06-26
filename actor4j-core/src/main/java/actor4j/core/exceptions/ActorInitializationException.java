/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.core.exceptions;

public class ActorInitializationException extends RuntimeException {
	protected static final long serialVersionUID = -6130654059639276742L;

	public ActorInitializationException() {
		super("actor initialization failed");
	}
}
