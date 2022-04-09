/*
 * Copyright (c) 2015-2021, David A. Bauer. All rights reserved.
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
package io.actor4j.core.internal;

public class DefaultActorThreadFactory extends DefaultThreadFactory {
   
    public DefaultActorThreadFactory(String name) {
    	super(name);
    }

    public ActorThread newThread(InternalActorSystem system) {
    	ActorThread t = system.getActorThreadFactory().apply(group, name + "-worker-thread-" + index.getAndIncrement(), system);
    	
    	if (t.isDaemon())
    		t.setDaemon(false);
    	if (t.getPriority() != Thread.MAX_PRIORITY)
    		t.setPriority(Thread.MAX_PRIORITY);
	        
        return t;
    }
}
