/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.service.node.utils;

public class TransferActorMessage {
	public Object value;
	public int tag;
	public String source;
	public String dest;
	
	public TransferActorMessage() {
		super();
	}

	public TransferActorMessage(Object value, int tag, String source, String dest) {
		super();
		this.value = value;
		this.tag = tag;
		this.source = source;
		this.dest = dest;
	}

	@Override
	public String toString() {
		return "TransferActorMessage [value=" + value + ", tag=" + tag + ", source=" + source + ", dest=" + dest + "]";
	}
}