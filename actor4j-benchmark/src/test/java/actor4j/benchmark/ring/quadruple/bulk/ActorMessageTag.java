/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark.ring.quadruple.bulk;

public enum ActorMessageTag {
	MSG, RUN;
	
	public ActorMessageTag valueOf(int tag) {
		return values()[tag];
	}
}
