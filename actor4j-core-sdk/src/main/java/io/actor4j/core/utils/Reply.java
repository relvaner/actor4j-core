/*
 * Copyright (c) 2015-2026, David A. Bauer. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.actor4j.core.utils;

import java.util.UUID;

public record Reply(Object value, int tag, UUID interaction) {
	public static final int PENDING = -1;
	public static final int NONE    = -2;
	public static final int FAILURE = -3;
	
	private static final Reply REPLY_PENDING = Reply.of(null, PENDING, null);
	private static final Reply REPLY_NONE    = Reply.of(null, NONE, null);
	
	public record Failure(Object value, int tag) {
		public static final int UNSPECIFIED = 0; // e.g. mapped to PodStatus.INTERNAL_SERVER_ERROR
	}
	
	public static Reply of(Object value, int tag, UUID interaction) {
		return new Reply(value, tag, interaction);
	}
	
	public static Reply of(Object value, int tag) {
		return of(value, tag, null);
	}
	
	public static Reply failure(Object value, int tag, UUID interaction) {
		return new Reply(new Failure(value, tag), FAILURE, interaction);
	}
	
	public static Reply failure(Object value, int tag) {
		return failure(value, tag, null);
	}
	
	public static Reply failure(Throwable t, int tag, UUID interaction) {
		return new Reply(new Failure(t, tag), FAILURE, interaction);
	}
	
	public static Reply failure(Throwable t, UUID interaction) {
		return failure(t, Failure.UNSPECIFIED, interaction);
	}
	
	public static Reply failure(Throwable t) {
		return failure(t, Failure.UNSPECIFIED, null);
	}
	
	public static Reply pending() {
		return REPLY_PENDING;
	}
	
	public static Reply none() {
		return REPLY_NONE;
	}
	
	public boolean isPending() {
        return tag==PENDING;
    }
	
	public boolean isNone() {
        return tag==NONE;
    }
	
	public boolean isFailure() {
        return tag==FAILURE;
    }
	
	public boolean isDone() {
		return tag>=0;
	}
}
