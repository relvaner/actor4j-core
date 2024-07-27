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
package io.actor4j.core.pods.utils;

import static io.actor4j.core.messages.ActorReservedTag.*;

public final class PodRequestMethod {
	public static final int OFFSET      = RESERVED_POD_REQUEST_METHOD_RANGE_START;
	
	public static final int GET         = OFFSET;
	public static final int GET_ALL     = OFFSET+1;
	public static final int POST        = OFFSET+2;
	public static final int PUT         = OFFSET+3;
	public static final int DELETE      = OFFSET+4;
	
	public static final int SUBSCRIBE   = OFFSET+6;
	public static final int UNSUBSCRIBE = OFFSET+7;
	public static final int PUBLISH     = OFFSET+8;
	
	public static final int ACTION_1    = OFFSET+11;
	public static final int ACTION_2    = OFFSET+12;
	public static final int ACTION_3    = OFFSET+13;
	public static final int ACTION_4    = OFFSET+14;
	public static final int ACTION_5    = OFFSET+15;
	public static final int ACTION_6    = OFFSET+16;
	public static final int ACTION_7    = OFFSET+17;
	public static final int ACTION_8    = OFFSET+18;
	public static final int ACTION_9    = OFFSET+19;
	public static final int ACTION_10   = OFFSET+20;
	
	public static final int ACTION_11   = OFFSET+21;
	public static final int ACTION_12   = OFFSET+22;
	public static final int ACTION_13   = OFFSET+23;
	public static final int ACTION_14   = OFFSET+24;
	public static final int ACTION_15   = OFFSET+25;
	public static final int ACTION_16   = OFFSET+26;
	public static final int ACTION_17   = OFFSET+27;
	public static final int ACTION_18   = OFFSET+28;
	public static final int ACTION_19   = OFFSET+29;
	public static final int ACTION_20   = OFFSET+30;
	
	public static final int COMMAND_1   = OFFSET+101;
	public static final int COMMAND_2   = OFFSET+102;
	public static final int COMMAND_3   = OFFSET+103;
	public static final int COMMAND_4   = OFFSET+104;
	public static final int COMMAND_5   = OFFSET+105;
	public static final int COMMAND_6   = OFFSET+106;
	public static final int COMMAND_7   = OFFSET+107;
	public static final int COMMAND_8   = OFFSET+108;
	public static final int COMMAND_9   = OFFSET+109;
	public static final int COMMAND_10  = OFFSET+110;
	
	public static final int COMMAND_11  = OFFSET+111;
	public static final int COMMAND_12  = OFFSET+112;
	public static final int COMMAND_13  = OFFSET+113;
	public static final int COMMAND_14  = OFFSET+114;
	public static final int COMMAND_15  = OFFSET+115;
	public static final int COMMAND_16  = OFFSET+116;
	public static final int COMMAND_17  = OFFSET+117;
	public static final int COMMAND_18  = OFFSET+118;
	public static final int COMMAND_19  = OFFSET+119;
	public static final int COMMAND_20  = OFFSET+120;
	
	public static final int QUERY_1     = OFFSET+201;
	public static final int QUERY_2     = OFFSET+202;
	public static final int QUERY_3     = OFFSET+203;
	public static final int QUERY_4     = OFFSET+204;
	public static final int QUERY_5     = OFFSET+205;
	public static final int QUERY_6     = OFFSET+206;
	public static final int QUERY_7     = OFFSET+207;
	public static final int QUERY_8     = OFFSET+208;
	public static final int QUERY_9     = OFFSET+209;
	public static final int QUERY_10    = OFFSET+210;
	
	public static final int QUERY_11    = OFFSET+211;
	public static final int QUERY_12    = OFFSET+212;
	public static final int QUERY_13    = OFFSET+213;
	public static final int QUERY_14    = OFFSET+214;
	public static final int QUERY_15    = OFFSET+215;
	public static final int QUERY_16    = OFFSET+216;
	public static final int QUERY_17    = OFFSET+217;
	public static final int QUERY_18    = OFFSET+218;
	public static final int QUERY_19    = OFFSET+219;
	public static final int QUERY_20    = OFFSET+220;
	
	public static final int EVENT_1     = OFFSET+301;
	public static final int EVENT_2     = OFFSET+302;
	public static final int EVENT_3     = OFFSET+303;
	public static final int EVENT_4     = OFFSET+304;
	public static final int EVENT_5     = OFFSET+305;
	public static final int EVENT_6     = OFFSET+306;
	public static final int EVENT_7     = OFFSET+307;
	public static final int EVENT_8     = OFFSET+308;
	public static final int EVENT_9     = OFFSET+309;
	public static final int EVENT_10    = OFFSET+310;
	
	public static final int EVENT_11    = OFFSET+311;
	public static final int EVENT_12    = OFFSET+312;
	public static final int EVENT_13    = OFFSET+313;
	public static final int EVENT_14    = OFFSET+314;
	public static final int EVENT_15    = OFFSET+315;
	public static final int EVENT_16    = OFFSET+316;
	public static final int EVENT_17    = OFFSET+317;
	public static final int EVENT_18    = OFFSET+318;
	public static final int EVENT_19    = OFFSET+319;
	public static final int EVENT_20    = OFFSET+320;
}