/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.core.messages;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FutureActorMessage<T> extends ActorMessage<T> {
	protected static final long serialVersionUID = -3161942153771543664L;
	
	public CompletableFuture<T> future;

	public FutureActorMessage(CompletableFuture<T> future, T value, int tag, UUID source, UUID dest) {
		super(value, tag, source, dest);
		this.future = future;
	}

	public FutureActorMessage(CompletableFuture<T> future, T value, Enum<?> tag, UUID source, UUID dest) {
		this(future, value, tag.ordinal(), source, dest);
	}
}
