/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.core.publish.subscribe;

public class Publish<T> extends Topic {
	public T value;
	
	public Publish(String topic, T value) {
		super(topic);
		this.value = value;
	}
}
