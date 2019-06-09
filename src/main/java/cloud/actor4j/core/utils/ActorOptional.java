/*
 * Copyright (c) 2015-2018, David A. Bauer. All rights reserved.
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
package cloud.actor4j.core.utils;

import java.util.Optional;

public final class ActorOptional<T> {
	protected Optional<T> optional;
	
	private ActorOptional() {
		// empty
	}
	
	private ActorOptional(Optional<T> optional) {
		this.optional = optional;
	}
	
	public static<T> ActorOptional<T> none() {
		return new ActorOptional<>();
	}
	
	public static<T> ActorOptional<T> empty() {
		return new ActorOptional<>(Optional.empty());
	}
	
	public static <T> ActorOptional<T> of(T value) {
		return new ActorOptional<>(Optional.ofNullable(value));
	}
	
	public T get() {
		return isDone() ? (optional.isPresent() ? optional.get() : null) : null;
	}
	
	public boolean isPresent() {
		return isDone() ? optional.isPresent() : false;
	}
	
	public boolean isDone() {
		return optional!=null;
	}
}