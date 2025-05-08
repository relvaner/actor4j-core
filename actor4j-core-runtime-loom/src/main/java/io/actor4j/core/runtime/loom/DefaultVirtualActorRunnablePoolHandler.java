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
package io.actor4j.core.runtime.loom;

import io.actor4j.core.runtime.InternalActorCell;
import io.actor4j.core.runtime.InternalActorSystem;

public class DefaultVirtualActorRunnablePoolHandler extends VirtualActorRunnablePoolHandler {
	public DefaultVirtualActorRunnablePoolHandler(InternalActorSystem system, VirtualActorRunnablePool virtualActorRunnablePool) {
		super(system, virtualActorRunnablePool);
	}

	@Override
	public VirtualActorRunnable createVirtualActorRunnable(InternalActorSystem system, InternalActorCell cell, Runnable onTermination) {
		return new DefaultVirtualActorRunnable(system, cell, onTermination);
	}
}
