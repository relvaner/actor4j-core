/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import io.actor4j.core.id.ActorId;

public class ConcurrentActorGroupSet implements ActorGroup {
	protected static final long serialVersionUID = 1L;

	protected final Set<ActorId> set;
	protected final UUID id;

	public ConcurrentActorGroupSet() {
		super();

		set = ConcurrentHashMap.newKeySet();
		id = UUID.randomUUID();
	}

	public ConcurrentActorGroupSet(Collection<ActorId> c) {
		super();

		Map<ActorId, Boolean> map = new ConcurrentHashMap<>((Map<ActorId, Boolean>)c.stream().collect(Collectors.toMap(v -> v, v -> false)));
		set = map.keySet();
		id = UUID.randomUUID();
	}

	public ConcurrentActorGroupSet(int initialCapacity, float loadFactor) {
		super();

		Map<ActorId, Boolean> map = new ConcurrentHashMap<>(initialCapacity, loadFactor);
		set = map.keySet();
		id = UUID.randomUUID();
	}

	public ConcurrentActorGroupSet(int initialCapacity) {
		super();

		Map<ActorId, Boolean> map = new ConcurrentHashMap<>(initialCapacity);
		set = map.keySet();
		id = UUID.randomUUID();
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public int size() {
		return set.size();
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return set.contains(o);
	}

	@Override
	public Iterator<ActorId> iterator() {
		return set.iterator();
	}

	@Override
	public Object[] toArray() {
		return set.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return set.toArray(a);
	}

	@Override
	public boolean add(ActorId e) {
		return set.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return set.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return set.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends ActorId> c) {
		return set.addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return set.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return set.retainAll(c);
	}

	@Override
	public void clear() {
		set.clear();
	}
}
