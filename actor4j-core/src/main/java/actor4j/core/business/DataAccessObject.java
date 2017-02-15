/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.core.business;

import java.util.UUID;

import org.bson.Document;

public class DataAccessObject<K, V> {
	public K key;
	public V value;
	public Document filter;
	public String collectionName;
	public UUID source;
	
	public Object reserved;
	
	public DataAccessObject(K key, V value, Document filter, String collectionName, UUID source) {
		super();
		this.key = key;
		this.value = value;
		this.filter = filter;
		this.collectionName = collectionName;
		this.source = source;
	}
}
