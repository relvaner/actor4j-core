/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark.ping.pong.grouped.bulk;

public enum ActorMessageTag {
	MSG, RUN;
	
	public ActorMessageTag valueOf(int tag) {
		return values()[tag];
	}
}
