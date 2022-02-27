package io.actor4j.core.messages;

import java.util.UUID;

import io.actor4j.core.utils.Copyable;
import io.actor4j.core.utils.Shareable;

public record DefaultActorMessage<T>(T value, int tag, UUID source, UUID dest, UUID interaction, String protocol, String domain) implements ActorMessage<T> {
	public DefaultActorMessage {
		// empty
	}

	public DefaultActorMessage(T value, int tag, UUID source, UUID dest) {
		this(value, tag, source, dest, null, null, null);
	}
	
	public DefaultActorMessage(T value, int tag, UUID source, UUID dest, String domain) {
		this(value, tag, source, dest, null, null, domain);
	}
	
	public DefaultActorMessage(T value, int tag, UUID source, UUID dest, UUID interaction) {
		this(value, tag, source, dest, interaction, null, null);
	}
	
	public DefaultActorMessage(T value, int tag, UUID source, UUID dest, UUID interaction, String protocol) {
		this(value, tag, source, dest, interaction, protocol, null);
	}

	public DefaultActorMessage(T value, Enum<?> tag, UUID source, UUID dest) {
		this(value, tag.ordinal(), source, dest);
	}
	
	public DefaultActorMessage(T value, Enum<?> tag, UUID source, UUID dest, String domain) {
		this(value, tag.ordinal(), source, dest, domain);
	}
	
	public DefaultActorMessage(T value, Enum<?> tag, UUID source, UUID dest, UUID interaction) {
		this(value, tag.ordinal(), source, dest, interaction);
	}
	
	public DefaultActorMessage(T value, Enum<?> tag, UUID source, UUID dest, UUID interaction, String protocol) {
		this(value, tag.ordinal(), source, dest, interaction, protocol);
	}
	
	public DefaultActorMessage(T value, Enum<?> tag, UUID source, UUID dest, UUID interaction, String protocol, String domain) {
		this(value, tag.ordinal(), source, dest, interaction, protocol, domain);
	}
	
	@Override
	public ActorMessage<T> weakCopy() {
		return new DefaultActorMessage<T>(value, tag, source, dest, interaction, protocol, domain);
	}
	
	@Override
	public ActorMessage<T> weakCopy(UUID source, UUID dest) {
		return this.source!=source || this.dest!=dest ? new DefaultActorMessage<T>(value, tag, source, dest, interaction, protocol, domain) : this;
	}
	
	@Override
	public ActorMessage<T> weakCopy(UUID dest) {
		return this.dest!=dest ? new DefaultActorMessage<T>(value, tag, source, dest, interaction, protocol, domain) : this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ActorMessage<T> copy() {
		if (value!=null) { 
			if (ActorMessageUtils.isSupportedType(value.getClass()) || value instanceof Shareable)
				return this;
			else if (value instanceof Copyable)
				return ActorMessage.create(((Copyable<T>)value).copy(), tag, source, dest, interaction, protocol, domain);
			else if (value instanceof Exception)
				return this;
			else
				throw new IllegalArgumentException(value.getClass().getName());
		}
		else
			return this;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ActorMessage<T> copy(UUID dest) {
		if (value!=null) { 
			if (ActorMessageUtils.isSupportedType(value.getClass()) || value instanceof Shareable)
				return dest()!=dest ? ActorMessage.create(value, tag, source, dest, interaction, protocol, domain) : this;
			else if (value instanceof Copyable)
				return ActorMessage.create(((Copyable<T>)value).copy(), tag, source, dest, interaction, protocol, domain);
			else if (value instanceof Exception)
				return dest()!=dest ? ActorMessage.create(value, tag, source, dest, interaction, protocol, domain) : this;
			else
				throw new IllegalArgumentException(value.getClass().getName());
		}
		else
			return dest()!=dest ? ActorMessage.create(null, tag, source, dest, interaction, protocol, domain) : this;
	}

	@Override
	public String toString() {
		return "ActorMessage [value=" + value + ", tag=" + tag + ", source=" + source + ", dest=" + dest
				+ ", interaction=" + interaction + ", protocol=" + protocol + ", domain=" + domain + "]";
	}
}
