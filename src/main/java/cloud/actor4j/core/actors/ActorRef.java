/*
 * Copyright (c) 2015-2018, David A. Bauer. All rights reserved.
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
package cloud.actor4j.core.actors;

import java.util.Queue;
import java.util.UUID;

import cloud.actor4j.core.ActorServiceNode;
import cloud.actor4j.core.ActorSystem;
import cloud.actor4j.core.messages.ActorMessage;

public interface ActorRef {
	public ActorSystem getSystem();
	
	public String getName();
	public UUID getId();
	public UUID self();
	public String getPath();
	public UUID getParent();
	public Queue<UUID> getChildren();
	public boolean isRoot();
	public boolean isRootInUser();
	
	public void send(ActorMessage<?> message);
	public void sendViaPath(ActorMessage<?> message, String path);
	public void sendViaPath(ActorMessage<?> message, String nodeName, String path);
	public void sendViaPath(ActorMessage<?> message, ActorServiceNode node, String path);
	public void sendViaAlias(ActorMessage<?> message, String alias);
	public void send(ActorMessage<?> message, UUID dest);
	public <T> void tell(T value, int tag, UUID dest);
	public <T> void tell(T value, int tag, String alias);
	public void forward(ActorMessage<?> message, UUID dest);
	public void priority(ActorMessage<?> message);
	public <T> void priority(T value, int tag, UUID dest);
	
	public void watch(UUID dest);
	public void unwatch(UUID dest);
}
