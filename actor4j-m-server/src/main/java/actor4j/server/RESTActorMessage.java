/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.server;

public class RESTActorMessage {
	public Object value;
	public int tag;
	public String source;
	public String dest;
	
	public RESTActorMessage() {
		super();
	}

	public RESTActorMessage(Object value, int tag, String source, String dest) {
		super();
		this.value = value;
		this.tag = tag;
		this.source = source;
		this.dest = dest;
	}

	@Override
	public String toString() {
		return "RESTActorMessage [value=" + value + ", tag=" + tag + ", source=" + source + ", dest=" + dest + "]";
	}
}