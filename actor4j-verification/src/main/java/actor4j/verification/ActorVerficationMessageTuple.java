/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.verification;

import java.util.List;

import actor4j.core.messages.ActorMessage;

public class ActorVerficationMessageTuple {
	protected List<ActorMessage<?>> messages;
	protected List<String> aliases;
	
	public ActorVerficationMessageTuple(List<ActorMessage<?>> messages, List<String> aliases) {
		super();
		this.messages = messages;
		this.aliases = aliases;
	}

	public List<ActorMessage<?>> getMessages() {
		return messages;
	}
	
	public void setMessages(List<ActorMessage<?>> messages) {
		this.messages = messages;
	}
	
	public List<String> getAliases() {
		return aliases;
	}
	
	public void setAliases(List<String> aliases) {
		this.aliases = aliases;
	}

	@Override
	public String toString() {
		return "ActorVerficationMessageTuple [messages=" + messages + ", aliases=" + aliases + "]";
	}
}
