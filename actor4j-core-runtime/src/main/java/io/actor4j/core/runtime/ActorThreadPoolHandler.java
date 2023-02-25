/*
 * Copyright (c) 2015-2019, David A. Bauer. All rights reserved.
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
package io.actor4j.core.runtime;

import java.util.UUID;
import java.util.function.BiConsumer;

import io.actor4j.core.messages.ActorMessage;

public class ActorThreadPoolHandler extends AbstractActorProcessPoolHandler<ActorThread> {
	public ActorThreadPoolHandler(InternalActorSystem system) {
		super(system);
	}
	
	public void unsafe_call(ActorMessage<?> message, UUID dest, ActorThread t) {
		InternalActorCell cell = system.getCells().get(dest);
		if (cell!=null) {
			cell.getRequestRate().getAndIncrement();
			t.failsafeOperationalMethod(message, cell);
		}
		if (system.getConfig().counterEnabled().get())
			t.counter.getAndIncrement();
	}
	
	public boolean unsafe_postInnerOuter(ActorMessage<?> message, UUID source) {
		boolean result = false; 
		
		if (system.getConfig().parallelism()==1 && system.getConfig().parallelismFactor()==1 && Thread.currentThread() instanceof ActorThread) {
			ActorThread t = ((ActorThread)Thread.currentThread());
			unsafe_call(message.copy(), message.dest(), t);
			result = true;
		}
		else {
			Long id_source = cellsMap.get(source);
			Long id_dest   = cellsMap.get(message.dest());
		
			if (id_dest!=null) {
				ActorThread t = processMap.get(id_dest);
				
				if (id_source!=null && id_source.equals(id_dest)
						&& Thread.currentThread().getId()==id_source.longValue()) {
					unsafe_call(message.copy(), message.dest(), t);	
				}
				else {
					t.outerQueue(message.copy());
					t.newMessage();
				}
				
				result = true;
			}
			else
				system.getMessageDispatcher().undelivered(message, source, message.dest());
		}
		
		return result;
	}
	
	public boolean unsafe_postInnerOuter(ActorMessage<?> message, UUID source, UUID dest) {
		boolean result = false;
		
		if (system.getConfig().parallelism()==1 && system.getConfig().parallelismFactor()==1 && Thread.currentThread() instanceof ActorThread) {
			ActorThread t = ((ActorThread)Thread.currentThread());
			unsafe_call(message.copy(dest), dest, t);
			result = true;
		}
		else {
			Long id_source = cellsMap.get(source);
			Long id_dest   = cellsMap.get(dest);
		
			if (id_dest!=null) {
				ActorThread t = processMap.get(id_dest);
				
				if (id_source!=null && id_source.equals(id_dest)
						&& Thread.currentThread().getId()==id_source.longValue())
					unsafe_call(message.copy(dest), dest, t);
				else {
					t.outerQueue(message.copy(dest));
					t.newMessage();
				}
				
				result = true;
			}	
			else
				system.getMessageDispatcher().undelivered(message, source, dest);
		}
		
		return result;
	}
	
	public boolean postInnerOuter(ActorMessage<?> message, UUID source) {
		boolean result = false;
		
		if (system.getConfig().parallelism()==1 && system.getConfig().parallelismFactor()==1 && Thread.currentThread() instanceof ActorThread) {
			ActorThread t = ((ActorThread)Thread.currentThread());
			t.innerQueue(message.copy());
			t.newMessage();
			result = true;
		}
		else {
			Long id_source = cellsMap.get(source);
			Long id_dest   = cellsMap.get(message.dest());
		
			if (id_dest!=null) {
				ActorThread t = processMap.get(id_dest);
				
				if (id_source!=null && id_source.equals(id_dest)
						&& Thread.currentThread().getId()==id_source.longValue())
					t.innerQueue(message.copy());
				else
					t.outerQueue(message.copy());
				
				t.newMessage();
				result = true;
			}
			else
				system.getMessageDispatcher().undelivered(message, source, message.dest());
		}
		
		return result;
	}
	
	public boolean postInnerOuter(ActorMessage<?> message, UUID source, UUID dest) {
		boolean result = false;
		
		if (system.getConfig().parallelism()==1 && system.getConfig().parallelismFactor()==1 && Thread.currentThread() instanceof ActorThread) {
			ActorThread t = ((ActorThread)Thread.currentThread());
			t.innerQueue(message.copy(dest));
			t.newMessage();
			result = true;
		}
		else {
			Long id_source = cellsMap.get(source);
			Long id_dest   = cellsMap.get(dest);
		
			if (id_dest!=null) {
				ActorThread t = processMap.get(id_dest);
				
				if (id_source!=null && id_source.equals(id_dest)
						&& Thread.currentThread().getId()==id_source.longValue())
					t.innerQueue(message.copy(dest));
				else
					t.outerQueue(message.copy(dest));
				
				t.newMessage();
				result = true;
			}	
			else
				system.getMessageDispatcher().undelivered(message, source, dest);
		}
		
		return result;
	}
	
	public boolean postOuter(ActorMessage<?> message) {
		Long id_dest = cellsMap.get(message.dest());
		if (id_dest!=null) {
			ActorThread t = processMap.get(id_dest);
			t.outerQueue(message.copy());
			t.newMessage();
		}
		
		return id_dest!=null;
	}
	
	public boolean postOuter(ActorMessage<?> message, UUID dest) {
		Long id_dest = cellsMap.get(dest);
		if (id_dest!=null) {
			ActorThread t = processMap.get(id_dest);
			t.outerQueue(message.copy(dest));
			t.newMessage();
		}
		
		return id_dest!=null;
	}
	
	public boolean postServer(ActorMessage<?> message) {
		Long id_dest = cellsMap.get(message.dest());
		if (id_dest!=null) {
			ActorThread t = processMap.get(id_dest);
			t.serverQueue(message.copy());
			t.newMessage();
		}
		
		return id_dest!=null;
	}
	
	public boolean postServer(ActorMessage<?> message, UUID dest) {
		Long id_dest = cellsMap.get(dest);
		if (id_dest!=null) {
			ActorThread t = processMap.get(id_dest);
			t.serverQueue(message.copy(dest));
			t.newMessage();
		}
		
		return id_dest!=null;
	}
	
	public boolean postQueue(ActorMessage<?> message, BiConsumer<ActorThread, ActorMessage<?>> biconsumer) {
		Long id_dest = cellsMap.get(message.dest());
		if (id_dest!=null) {
			ActorThread t = processMap.get(id_dest);
			biconsumer.accept(t, message.copy());
			t.newMessage();
		}
		
		return id_dest!=null;
	}
	
	public boolean postQueue(ActorMessage<?> message, UUID dest, BiConsumer<ActorThread, ActorMessage<?>> biconsumer) {
		Long id_dest = cellsMap.get(dest);
		if (id_dest!=null) {
			ActorThread t = processMap.get(id_dest);
			biconsumer.accept(t, message.copy(dest));
			t.newMessage();
		}
		
		return id_dest!=null;
	}
}
