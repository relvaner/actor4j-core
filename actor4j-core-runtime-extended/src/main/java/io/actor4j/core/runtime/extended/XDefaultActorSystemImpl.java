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
package io.actor4j.core.runtime.extended;

import java.util.Queue;

import org.jctools.queues.MpscArrayQueue;

import io.actor4j.core.XActorService;
import io.actor4j.core.config.XActorServiceConfig;
import io.actor4j.core.config.XActorSystemConfig;
import io.actor4j.core.runtime.DefaultActorMessageDispatcher;
import io.actor4j.core.runtime.DefaultActorSystemImpl;

public class XDefaultActorSystemImpl extends DefaultActorSystemImpl implements XActorService {
	public XDefaultActorSystemImpl() {
		this(null);
	}

	public XDefaultActorSystemImpl(XActorSystemConfig config) {
		super(config!=null ? config : (config=XActorSystemConfig.create()));
		
//		container = DefaultDIContainer.create(); // override
//		podReplicationController = new XPodReplicationController(this); // override
		
		messageDispatcher = new DefaultActorMessageDispatcher(this);
		
		setActorThread(config.unbounded());
	}
	
	@Override
	public <T> Queue<T> createLockFreeLinkedQueue() {
		return new MpscArrayQueue<>(64);
	}
	
	@Override
	public boolean setConfig(XActorSystemConfig config) {
		boolean result = false;
		
		result = super.setConfig(config);
		if (result)
			setActorThread(((XActorSystemConfig)config).unbounded());
			
		return result;
	}
	
	@Override
	public boolean setConfig(XActorServiceConfig config) {
		return setConfig((XActorSystemConfig)config);
	}
	
	public void setActorThread(boolean unbounded) {
		if (unbounded)
			actorThreadFactory = (group, n, system) -> new UnboundedActorThread(group, n, system);
		else
			actorThreadFactory = (group, n, system) -> new BoundedActorThread(group, n, system);
	}
	
//	@Override
//	public List<ActorId> addActor(int instances, Class<? extends Actor> clazz, Object... args) throws ActorInitializationException {
//		List<ActorId> result = new ArrayList<>(instances);
//		
//		for (int i=0; i<instances; i++)
//			result.add(addActor(clazz, args));
//		
//		return result;	
//	}
//	
//	@Override
//	public ActorId addActor(Class<? extends Actor> clazz, Object... args) throws ActorInitializationException {
//		InternalActorCell cell = generateCell(clazz);
//		((DefaultDIContainer<ActorId>)container).registerConstructorInjector(cell.getId(), clazz, args);
//		Actor actor = null;
//		try {
//			actor = (Actor)container.getInstance(cell.getId());
//			cell.setActor(actor);
//		} catch (Exception e) {
//			e.printStackTrace();
//			executorService.getFaultToleranceManager().notifyErrorHandler(new ActorInitializationException(), ActorSystemError.ACTOR_INITIALIZATION, null);
//		}
//		
//		return (actor!=null) ? user_addCell(cell) : ZERO_ID;
//	}
}
