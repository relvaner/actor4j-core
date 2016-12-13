/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.verification;

import java.util.List;
import java.util.Set;

public class ActorVerficationMessageTuple {
	protected Set<Integer> events;
	protected List<String> aliases;
	
	public ActorVerficationMessageTuple(Set<Integer> events, List<String> aliases) {
		super();
		this.events = events;
		this.aliases = aliases;
	}

	public Set<Integer> getEvents() {
		return events;
	}

	public void setEvents(Set<Integer> events) {
		this.events = events;
	}

	public List<String> getAliases() {
		return aliases;
	}

	public void setAliases(List<String> aliases) {
		this.aliases = aliases;
	}

	@Override
	public String toString() {
		return "ActorVerficationMessageTuple [events=" + events + ", aliases=" + aliases + "]";
	}
}
