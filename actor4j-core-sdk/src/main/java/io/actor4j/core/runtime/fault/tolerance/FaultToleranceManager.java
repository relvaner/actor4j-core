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
package io.actor4j.core.runtime.fault.tolerance;

import io.actor4j.core.runtime.ActorSystemError;

//Adapted for actor4j
public final class FaultToleranceManager {
	protected /*final*/ ErrorHandler errorHandler; //TODO: change to final, adapt test case
	
	public FaultToleranceManager(ErrorHandler errorHandler) {
		super();
		this.errorHandler = errorHandler;
	}

	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}
	
	public void notifyErrorHandler(Throwable t, ActorSystemError systemError, Object faultToleranceId) {
		notifyErrorHandler(t, systemError, "", faultToleranceId);
	}

	public synchronized void notifyErrorHandler(Throwable t, ActorSystemError systemError, String message, Object faultToleranceId) {
		if (errorHandler!=null)
			errorHandler.handle(t, systemError, message, faultToleranceId);
	}
}
