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
package io.actor4j.core.pods.functions.utils;

import io.actor4j.core.pods.utils.PodStatus;
import io.actor4j.core.utils.Reply;
import io.actor4j.core.utils.Reply.Failure;

// unwraps a failure reply into a final reply with the tag of the failure
public final class PodFailures {
	private PodFailures() {
	}

	// remote: a Throwable is not sent over the wire, only its message (or class name)
	public static Reply unwrap(Reply result, boolean remote) {
		Failure failure = (Failure)result.value();
		int tag = failure.tag()!=Failure.UNSPECIFIED ? failure.tag() : PodStatus.INTERNAL_SERVER_ERROR;
		Object value = failure.value();
		if (remote && value instanceof Throwable t)
			value = t.getMessage()!=null ? t.getMessage() : t.getClass().getName();

		return Reply.of(value, tag, result.interaction());
	}
}
