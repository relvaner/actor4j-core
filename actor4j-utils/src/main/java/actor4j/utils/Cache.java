/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.utils;

public interface Cache<K, E> {
	public E get(K key);
	public E put(K key, E value);
	public void remove(K key);
	
	public void gc(long maxTime);
}
