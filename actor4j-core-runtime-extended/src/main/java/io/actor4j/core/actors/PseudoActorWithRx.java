/*
 * Copyright (c) 2015-2024, David A. Bauer. All rights reserved.
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
package io.actor4j.core.actors;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.InternalPseudoActorCell;
import io.actor4j.core.utils.ActorMessageFlowable;
import io.reactivex.rxjava3.core.Flowable;

public abstract class PseudoActorWithRx extends PseudoActor {
	protected final Flowable<ActorMessage<?>> rxOuterQueueL1;
	
	public PseudoActorWithRx(ActorSystem system, boolean blocking) {
		this(null, system, blocking);
	}
	
	public PseudoActorWithRx(String name, ActorSystem system, boolean blocking) {
		super(name, system, blocking);
		rxOuterQueueL1 = ActorMessageFlowable.getMessages(((InternalPseudoActorCell)getCell()).getOuterQueueL1());
	}
	
	public Flowable<ActorMessage<?>> runWithRx() {
		boolean hasNextOuter = ((InternalPseudoActorCell)cell).getOuterQueueL1().peek()!=null;
		if (!hasNextOuter && ((InternalPseudoActorCell)cell).getOuterQueueL2().peek()!=null) {
			ActorMessage<?> message = null;
			for (int j=0; (message=((InternalPseudoActorCell)cell).getOuterQueueL2().poll())!=null && j<((InternalPseudoActorCell)cell).getSystem().getConfig().bufferQueueSize(); j++)
				((InternalPseudoActorCell)cell).getOuterQueueL1().offer(message);
		}
		
		return rxOuterQueueL1;
	}
}
