/*
 * Copyright (c) 2015-2022, David A. Bauer. All rights reserved.
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

import java.util.List;
import java.util.Set;

import io.actor4j.core.ActorServiceNode;
import io.actor4j.core.internal.failsafe.FailsafeManager;
import io.actor4j.core.internal.persistence.ActorPersistenceService;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorTimer;

public interface ActorExecuterService {
	public FailsafeManager getFailsafeManager(); // TODO: abstract/interface
	public ActorThreadPool getActorThreadPool(); // TODO: abstract/interface
	public ActorPersistenceService getPersistenceService(); // TODO: abstract/interface
	
	public boolean isStarted();
	
	public ActorTimer timer();
	public ActorTimer globalTimer();
	
	public void run(Runnable onStartup);
	public void start(Runnable onStartup, Runnable onTermination);
	public void shutdown(boolean await);
	
	public void clientViaAlias(final ActorMessage<?> message, final String alias);
	public void clientViaPath(final ActorMessage<?> message, final ActorServiceNode node, final String path);
	public void resource(final ActorMessage<?> message);
	
	public long getCount();
	public List<Long> getCounts();
	
	public Set<Long> nonResponsiveThreads();
	public int nonResponsiveThreadsCount();
}
