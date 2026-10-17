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

import io.actor4j.core.XActorService;
import io.actor4j.core.config.XActorServiceConfig;
import io.actor4j.core.config.XActorSystemConfig;
import io.actor4j.core.jctools.JCToolsMessageQueueFactory;
import io.actor4j.core.runtime.DefaultActorMessageDispatcher;
import io.actor4j.core.runtime.DefaultActorSystemImpl;

public class XDefaultActorSystemImpl extends DefaultActorSystemImpl implements XActorService {
	public XDefaultActorSystemImpl() {
		this(null);
	}

	public XDefaultActorSystemImpl(XActorSystemConfig config) {
		super(config!=null ? config : (config=XActorSystemConfig.create()));
		
		messageQueueFactory = new JCToolsMessageQueueFactory();
		
//		podReplicationController = new XPodReplicationController(this); // override
		
		messageDispatcher = new DefaultActorMessageDispatcher(this);
		
		setActorThread(config.unbounded(), config.antiFlooding());
	}
	
	@Override
	public boolean setConfig(XActorSystemConfig config) {
		boolean result = false;
		
		result = super.setConfig(config);
		if (result)
			setActorThread(((XActorSystemConfig)config).unbounded(), ((XActorSystemConfig)config).antiFlooding());
			
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
	
	public void setActorThread(boolean unbounded, boolean antiFlooding) {
//		if (antiFlooding)
//			actorThreadFactory = (group, n, system) -> new AntiFloodingActorThread(group, n, system);
//		else 
			setActorThread(unbounded);
	}
}
