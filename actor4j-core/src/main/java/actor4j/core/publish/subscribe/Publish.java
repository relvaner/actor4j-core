/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.core.publish.subscribe;

public class Publish<T> extends Topic {
	public T value;
	
	public Publish(String topic, T value) {
		super(topic);
		this.value = value;
	}
}
