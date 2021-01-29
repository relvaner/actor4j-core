package io.actor4j.core;

import java.util.function.BiFunction;

public interface ActorSystemImplFactory extends BiFunction<String, ActorSystem, ActorSystemImpl>{
}
