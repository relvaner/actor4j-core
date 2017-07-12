/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.verification;

import java.util.List;
import java.util.Set;

public class ActorVerficationEdgeTuple {
	protected Set<Integer> events;
	protected List<String> aliases;
	
	public ActorVerficationEdgeTuple(Set<Integer> events, List<String> aliases) {
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
		return "ActorVerficationEdgeTuple [events=" + events + ", aliases=" + aliases + "]";
	}
}
