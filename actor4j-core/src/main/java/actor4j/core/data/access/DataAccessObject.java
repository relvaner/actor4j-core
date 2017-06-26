/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.core.data.access;

import java.util.UUID;

import org.bson.Document;

public class DataAccessObject<K, V> {
	public K key;
	public V value;
	public Document filter;
	public Document update;
	public String collectionName;
	public UUID source;
	
	public Object reserved;
	
	public DataAccessObject(K key, V value, Document filter, Document update, String collectionName, UUID source) {
		super();
		this.key = key;
		this.value = value;
		this.filter = filter;
		this.update = update;
		this.collectionName = collectionName;
		this.source = source;
	}

	@Override
	public String toString() {
		return "DataAccessObject [key=" + key + ", value=" + value + ", filter=" + filter + ", update=" + update
				+ ", collectionName=" + collectionName + ", source=" + source + ", reserved=" + reserved + "]";
	}
}
