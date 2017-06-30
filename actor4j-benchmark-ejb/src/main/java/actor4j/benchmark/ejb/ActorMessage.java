/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.benchmark.ejb;

import java.util.List;

public class ActorMessage {
	public Object value;
	public int tag;
	
	public ActorMessage(Object value, int tag) {
		super();
		this.value = value;
		this.tag = tag;
	}
	
	public ActorMessage(Object value, Enum<?> tag) {
		this(value, tag.ordinal());
	}
	
	public ActorMessage(int tag) {
		this(null, tag);
	}
	
	public ActorMessage(Enum<?> tag) {
		this(tag.ordinal());
	}
	
	public boolean valueAsBooolean() {
		return (boolean)value;
	}
	
	public int valueAsInt() {
		return (int)value;
	}
	
	public double valueAsDouble() {
		return (double)value;
	}
	
	public String valueAsString() {
		return (String)value;
	}
	
	public List<?> valueAsList() {
		return (List<?>)value;
	}
}
