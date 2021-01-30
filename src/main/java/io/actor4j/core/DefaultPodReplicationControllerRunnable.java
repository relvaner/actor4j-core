/*
 * Copyright (c) 2015-2020, David A. Bauer. All rights reserved.
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
package io.actor4j.core;

import static io.actor4j.core.logging.system.SystemActorLogger.systemLogger;

public class DefaultPodReplicationControllerRunnable extends PodReplicationControllerRunnable {
	public DefaultPodReplicationControllerRunnable(ActorSystemImpl system) {
		super(system);
	}

	@Override
	public void onRun() {
		horizontalPodAutoscaler();
	}
	
	public void horizontalPodAutoscaler() {
		systemLogger().debug(String.format("[REPLICATION][AUTOSCALER] sync"));
	}
}
