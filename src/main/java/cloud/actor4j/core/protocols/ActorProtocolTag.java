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
package cloud.actor4j.core.protocols;

public final class ActorProtocolTag {
	public static final int INTERNAL_RESTART             = -1;
	public static final int INTERNAL_STOP                = -2;
	public static final int INTERNAL_STOP_SUCCESS        = -3;
	public static final int INTERNAL_KILL                = -4;
	public static final int INTERNAL_RECOVER             = -5;
	
	public static final int INTERNAL_PERSISTENCE_RECOVER = -6;
	public static final int INTERNAL_PERSISTENCE_SUCCESS = -7;
	public static final int INTERNAL_PERSISTENCE_FAILURE = -8;
	
	public static final int INTERNAL_ACTIVATE            = -9;
	public static final int INTERNAL_DEACTIVATE          = -10;
}
