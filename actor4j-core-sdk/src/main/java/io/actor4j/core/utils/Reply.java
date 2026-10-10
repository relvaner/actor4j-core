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
	private static final Reply NONE    = Reply.of(null, -1, null);
	private static final Reply PENDING = Reply.of(null, -2, null);
	
	public static Reply of(Object value, int tag, UUID interaction) {
		return new Reply(value, tag, interaction);
	}
	
	public static Reply of(Object value, int tag) {
		return new Reply(value, tag, null);
	}
	
	public static Reply none() {
		return NONE;
	}
	
	public static Reply pending() {
		return PENDING;
	}
	
	public boolean isNone() {
        return tag == NONE.tag;
    }
	
	public boolean isPending() {
        return tag == PENDING.tag;
    }
	
	public boolean isDone() {
		return tag >= 0;
	}
}
