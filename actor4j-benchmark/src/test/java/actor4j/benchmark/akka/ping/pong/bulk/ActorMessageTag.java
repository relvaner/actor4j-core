package actor4j.benchmark.akka.ping.pong.bulk;

public enum ActorMessageTag {
	MSG, RUN;
	
	public ActorMessageTag valueOf(int tag) {
		return values()[tag];
	}
}
