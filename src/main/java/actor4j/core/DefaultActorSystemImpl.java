/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
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
package actor4j.core;

import java.util.ArrayList;
import java.util.List;

public class DefaultActorSystemImpl extends ActorSystemImpl {
	public DefaultActorSystemImpl(ActorSystem wrapper) {
		this(null, wrapper);
	}
	
	public DefaultActorSystemImpl(String name, ActorSystem wrapper) {
		super(name, wrapper);
		
		messageDispatcher = new DefaultActorMessageDispatcher(this);
		actorThreadClass  = DefaultActorThread.class;
	}
	
	public List<Integer> getWorkerInnerQueueSizes() {
		List<Integer> list = new ArrayList<>();
		for (ActorThread t : executerService.getActorThreads())
			list.add(((DefaultActorThread)t).getInnerQueue().size());
		return list;
	}
	
	public List<Integer> getWorkerOuterQueueSizes() {
		List<Integer> list = new ArrayList<>();
		for (ActorThread t : executerService.getActorThreads())
			list.add(((DefaultActorThread)t).getOuterQueue().size());
		return list;
	}
}
