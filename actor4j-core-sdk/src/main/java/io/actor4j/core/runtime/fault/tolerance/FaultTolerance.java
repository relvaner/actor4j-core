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

import java.util.UUID;

import io.actor4j.core.runtime.ActorSystemError;

public final class FaultTolerance {
	public static void run(final FaultToleranceManager faultToleranceManager, final ActorSystemError systemError, final String message, final FaultToleranceMethod method, Object faultToleranceId) {
		boolean error = false;
		Exception exception = null;
		
		try {
			method.run(faultToleranceId);
		}
		catch(Exception e) {
			if (message!=null)
				System.out.printf("Method failed: %s (UUID: %s)%n", message, faultToleranceId.toString());
			
			method.error(e);
			error = true;
			exception = e;
		}
		finally {
			method.postRun();
		}
		
		if (error)
			faultToleranceManager.notifyErrorHandler(exception, systemError, message, faultToleranceId);
	}
	
	public static void runAndCatchThrowable(final FaultToleranceManager faultToleranceManager, final ActorSystemError systemError, final String message, final FaultToleranceMethod method, Object faultToleranceId) {
		boolean error = false;
		Throwable throwable = null;
		
		try {
			method.run(faultToleranceId);
		}
		catch(Throwable t) {
			if (message!=null)
				System.out.printf("Method failed: %s (UUID: %s)%n", message, faultToleranceId.toString());
			
			method.error(t);
			error = true;
			throwable = t;
		}
		finally {
			method.postRun();
		}
		
		if (error)
			faultToleranceManager.notifyErrorHandler(throwable, systemError, message, faultToleranceId);
	}
	
	public static void run(final FaultToleranceManager faultToleranceManager, final ActorSystemError systemError, final FaultToleranceMethod method, Object faultToleranceId) {
		run(faultToleranceManager, systemError, "", method, faultToleranceId);
	}
	
	public static void runAndCatchThrowable(final FaultToleranceManager faultToleranceManager, final ActorSystemError systemError, final FaultToleranceMethod method, Object faultToleranceId) {
		runAndCatchThrowable(faultToleranceManager, systemError, "", method, faultToleranceId);
	}
	
	public static void run(final FaultToleranceManager faultToleranceManager, final FaultToleranceMethod method, UUID uuid) {
		run(faultToleranceManager, null, null, method, uuid);
	}
	
	public static void runAndCatchThrowable(final FaultToleranceManager faultToleranceManager, final FaultToleranceMethod method, UUID uuid) {
		runAndCatchThrowable(faultToleranceManager, null, null, method, uuid);
	}
}
