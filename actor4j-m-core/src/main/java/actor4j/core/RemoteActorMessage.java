package actor4j.core;

import java.util.LinkedHashMap;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RemoteActorMessage extends ActorMessage<LinkedHashMap<String, Object>> {
	protected static final long serialVersionUID = 6359184340878978613L;
	
	public RemoteActorMessage(LinkedHashMap<String, Object> value, int tag, UUID source, UUID dest) {
		super(value, tag, source, dest);
	}
	
	@SuppressWarnings("unchecked")
	public <C> C convertValue(Class<?> clazz) {
		return (C)(new ObjectMapper().convertValue(value, clazz));
	}
	
	@Override
	public RemoteActorMessage clone() {
		return new RemoteActorMessage(value, tag, source, dest);
	}

	@Override
	public String toString() {
		return "RemoteActorMessage [value=" + value + ", tag=" + tag + ", source=" + source + ", dest=" + dest + "]";
	}
}
