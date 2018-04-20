/*
 * safety4j - Safety Library
 * Copyright (c) 2014-2017, David A. Bauer
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package actor4j.core.safety;

import java.util.UUID;

public final class SafetyMethod {
	public static void run(final SafetyManager safetyManager, final String message, final Method method, UUID uuid) {
		boolean error = false;
		Exception exception = null;
		
		try {
			method.run(uuid);
		}
		catch(Exception e) {
			if (message!=null)
				System.out.printf("Method failed: %s (UUID: %s)%n", message, uuid.toString());
			
			method.error(e);
			error = true;
			exception = e;
		}
		finally {
			method.after();
		}
		
		if (error)
			safetyManager.notifyErrorHandler(exception, message, uuid);
	}
	
	public static void runAndCatchThrowable(final SafetyManager safetyManager, final String message, final Method method, UUID uuid) {
		boolean error = false;
		Throwable throwable = null;
		
		try {
			method.run(uuid);
		}
		catch(Throwable t) {
			if (message!=null)
				System.out.printf("Method failed: %s (UUID: %s)%n", message, uuid.toString());
			
			method.error(t);
			error = true;
			throwable = t;
		}
		finally {
			method.after();
		}
		
		if (error)
			safetyManager.notifyErrorHandler(throwable, message, uuid);
	}
	
	public static void run(final SafetyManager safetyManager, final Method method, UUID uuid) {
		run(safetyManager, null, method, uuid);
	}
	
	public static void runAndCatchThrowable(final SafetyManager safetyManager, final Method method, UUID uuid) {
		runAndCatchThrowable(safetyManager, null, method, uuid);
	}
}
