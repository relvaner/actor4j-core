package actor4j.core;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class ActorMessage<T> implements Serializable, Cloneable {
	protected static final long serialVersionUID = 365363944284831003L;
	
	public T value;
	public int tag;
	public UUID source;
	public UUID dest;
	
	public ActorMessage(T value, int tag, UUID source, UUID dest) {
		super();
		
		this.value = value;
		this.tag = tag;
		this.source = source;
		this.dest = dest;
	}

	public ActorMessage(T value, Enum<?> tag, UUID source, UUID dest) {
		this(value, tag.ordinal(), source, dest);
	}
	
	public T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		this.value = value;
	}
	
	public UUID getSource() {
		return source;
	}

	public void setSource(UUID source) {
		this.source = source;
	}

	public UUID getDest() {
		return dest;
	}

	public void setDest(UUID dest) {
		this.dest = dest;
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
	
	public UUID valueAsUUID() {
		return (UUID)value;
	}
	
	@Override
	public ActorMessage<T> clone() {
		return new ActorMessage<T>(value, tag, source, dest);
	}
	
	@Override
	public String toString() {
		return "ActorMessage [value=" + value + ", tag=" + tag + ", source=" + source
				+ ", dest=" + dest + "]";
	}
}
