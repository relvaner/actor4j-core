module io.actor4j.core {
	requires java.base;
	requires transitive java.logging;
	
	exports io.actor4j.core;
	exports io.actor4j.core.actors;
	exports io.actor4j.core.exceptions;
	exports io.actor4j.core.function;
	exports io.actor4j.core.immutable;
	exports io.actor4j.core.internal;
	exports io.actor4j.core.internal.annotations.concurrent;
	exports io.actor4j.core.internal.balancing;
	exports io.actor4j.core.internal.di;
	exports io.actor4j.core.internal.failsafe;
	exports io.actor4j.core.internal.persistence;
	exports io.actor4j.core.internal.persistence.actor;
	exports io.actor4j.core.internal.pods;
	exports io.actor4j.core.internal.protocols;
	exports io.actor4j.core.logging;
	exports io.actor4j.core.messages;
	exports io.actor4j.core.persistence;
	exports io.actor4j.core.persistence.connectors;
	exports io.actor4j.core.pods;
	exports io.actor4j.core.pods.actors;
	exports io.actor4j.core.pods.functions;
	exports io.actor4j.core.pods.utils;
	exports io.actor4j.core.service.discovery;
	exports io.actor4j.core.supervisor;
	exports io.actor4j.core.utils;
}