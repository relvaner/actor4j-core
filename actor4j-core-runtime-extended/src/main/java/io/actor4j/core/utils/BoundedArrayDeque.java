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

import java.util.ArrayDeque;

public class BoundedArrayDeque<E> extends ArrayDeque<E> {
	private static final long serialVersionUID = 8988969480168144122L;

	private final int capacity;
	
	public BoundedArrayDeque(int capacity) {
		super(capacity);
		this.capacity = capacity;
	}

	public int getCapacity() {
		return capacity;
	}

	@Override
	public boolean offer(E e) {
		return size()<capacity && super.offer(e);
	}
	
	@Override
	public boolean add(E e) {
		if (!offer(e))
			throw new IllegalStateException("Queue full");
		return true;
	}
}
