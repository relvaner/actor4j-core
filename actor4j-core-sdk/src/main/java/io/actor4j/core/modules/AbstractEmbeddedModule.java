/*
 * Copyright (c) 2015-2025, David A. Bauer. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.actor4j.core.modules;

import io.actor4j.core.actors.ActorRef;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.runtime.ActorSystemError;
import io.actor4j.core.runtime.InternalActorSystem;
import io.actor4j.core.runtime.fault.tolerance.FaultToleranceManager;

public abstract class AbstractEmbeddedModule implements EmbeddedModule {
	protected final String name;
	
	protected final ActorRef host;
	protected final PodContext context;
	
	protected final FaultToleranceManager hostFaultToleranceManager;
	
	public AbstractEmbeddedModule(String name, ActorRef host, PodContext context) {
		super();
		this.name = name;
		this.host = host;
		this.context = context;

		hostFaultToleranceManager = ((InternalActorSystem)host.getSystem()).getExecutorService().getFaultToleranceManager();
	}
	
	public String name() {
		return name;
	}
	
	@Override
	public void preStart() {
		// empty
	}

	@Override
	public boolean match(ActorMessage<?> message) {
		boolean result = false;
		
		try {
			result = onMatch(message);
		}
		catch(Exception e) {
			if (hostFaultToleranceManager!=null)
				hostFaultToleranceManager.notifyErrorHandler(e, ActorSystemError.EMBEDDED_MODULE, name, host.getId());
			fallback(message, e);
		}
		
		return result;
	}
	
	protected abstract boolean onMatch(ActorMessage<?> message);
	
	@Override
	public void fallback(ActorMessage<?> message, Exception e) {
		// empty
	}
	
	@Override
	public void postStop() {
		// empty
	}
}
