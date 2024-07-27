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
	public static final int GET         = RESERVED_POD_REQUEST_METHOD_TAG_MIN;
	public static final int GET_ALL     = RESERVED_POD_REQUEST_METHOD_TAG_MIN+1;
	public static final int POST        = RESERVED_POD_REQUEST_METHOD_TAG_MIN+2;
	public static final int PUT         = RESERVED_POD_REQUEST_METHOD_TAG_MIN+3;
	public static final int DELETE      = RESERVED_POD_REQUEST_METHOD_TAG_MIN+4;
	
	public static final int SUBSCRIBE   = RESERVED_POD_REQUEST_METHOD_TAG_MIN+6;
	public static final int UNSUBSCRIBE = RESERVED_POD_REQUEST_METHOD_TAG_MIN+7;
	public static final int PUBLISH     = RESERVED_POD_REQUEST_METHOD_TAG_MIN+8;
	
	public static final int ACTION_1    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+11;
	public static final int ACTION_2    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+12;
	public static final int ACTION_3    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+13;
	public static final int ACTION_4    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+14;
	public static final int ACTION_5    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+15;
	public static final int ACTION_6    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+16;
	public static final int ACTION_7    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+17;
	public static final int ACTION_8    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+18;
	public static final int ACTION_9    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+19;
	public static final int ACTION_10   = RESERVED_POD_REQUEST_METHOD_TAG_MIN+20;
	
	public static final int ACTION_11   = RESERVED_POD_REQUEST_METHOD_TAG_MIN+21;
	public static final int ACTION_12   = RESERVED_POD_REQUEST_METHOD_TAG_MIN+22;
	public static final int ACTION_13   = RESERVED_POD_REQUEST_METHOD_TAG_MIN+23;
	public static final int ACTION_14   = RESERVED_POD_REQUEST_METHOD_TAG_MIN+24;
	public static final int ACTION_15   = RESERVED_POD_REQUEST_METHOD_TAG_MIN+25;
	public static final int ACTION_16   = RESERVED_POD_REQUEST_METHOD_TAG_MIN+26;
	public static final int ACTION_17   = RESERVED_POD_REQUEST_METHOD_TAG_MIN+27;
	public static final int ACTION_18   = RESERVED_POD_REQUEST_METHOD_TAG_MIN+28;
	public static final int ACTION_19   = RESERVED_POD_REQUEST_METHOD_TAG_MIN+29;
	public static final int ACTION_20   = RESERVED_POD_REQUEST_METHOD_TAG_MIN+30;
	
	public static final int COMMAND_1   = RESERVED_POD_REQUEST_METHOD_TAG_MIN+101;
	public static final int COMMAND_2   = RESERVED_POD_REQUEST_METHOD_TAG_MIN+102;
	public static final int COMMAND_3   = RESERVED_POD_REQUEST_METHOD_TAG_MIN+103;
	public static final int COMMAND_4   = RESERVED_POD_REQUEST_METHOD_TAG_MIN+104;
	public static final int COMMAND_5   = RESERVED_POD_REQUEST_METHOD_TAG_MIN+105;
	public static final int COMMAND_6   = RESERVED_POD_REQUEST_METHOD_TAG_MIN+106;
	public static final int COMMAND_7   = RESERVED_POD_REQUEST_METHOD_TAG_MIN+107;
	public static final int COMMAND_8   = RESERVED_POD_REQUEST_METHOD_TAG_MIN+108;
	public static final int COMMAND_9   = RESERVED_POD_REQUEST_METHOD_TAG_MIN+109;
	public static final int COMMAND_10  = RESERVED_POD_REQUEST_METHOD_TAG_MIN+110;
	
	public static final int COMMAND_11  = RESERVED_POD_REQUEST_METHOD_TAG_MIN+111;
	public static final int COMMAND_12  = RESERVED_POD_REQUEST_METHOD_TAG_MIN+112;
	public static final int COMMAND_13  = RESERVED_POD_REQUEST_METHOD_TAG_MIN+113;
	public static final int COMMAND_14  = RESERVED_POD_REQUEST_METHOD_TAG_MIN+114;
	public static final int COMMAND_15  = RESERVED_POD_REQUEST_METHOD_TAG_MIN+115;
	public static final int COMMAND_16  = RESERVED_POD_REQUEST_METHOD_TAG_MIN+116;
	public static final int COMMAND_17  = RESERVED_POD_REQUEST_METHOD_TAG_MIN+117;
	public static final int COMMAND_18  = RESERVED_POD_REQUEST_METHOD_TAG_MIN+118;
	public static final int COMMAND_19  = RESERVED_POD_REQUEST_METHOD_TAG_MIN+119;
	public static final int COMMAND_20  = RESERVED_POD_REQUEST_METHOD_TAG_MIN+120;
	
	public static final int QUERY_1     = RESERVED_POD_REQUEST_METHOD_TAG_MIN+201;
	public static final int QUERY_2     = RESERVED_POD_REQUEST_METHOD_TAG_MIN+202;
	public static final int QUERY_3     = RESERVED_POD_REQUEST_METHOD_TAG_MIN+203;
	public static final int QUERY_4     = RESERVED_POD_REQUEST_METHOD_TAG_MIN+204;
	public static final int QUERY_5     = RESERVED_POD_REQUEST_METHOD_TAG_MIN+205;
	public static final int QUERY_6     = RESERVED_POD_REQUEST_METHOD_TAG_MIN+206;
	public static final int QUERY_7     = RESERVED_POD_REQUEST_METHOD_TAG_MIN+207;
	public static final int QUERY_8     = RESERVED_POD_REQUEST_METHOD_TAG_MIN+208;
	public static final int QUERY_9     = RESERVED_POD_REQUEST_METHOD_TAG_MIN+209;
	public static final int QUERY_10    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+210;
	
	public static final int QUERY_11    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+211;
	public static final int QUERY_12    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+212;
	public static final int QUERY_13    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+213;
	public static final int QUERY_14    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+214;
	public static final int QUERY_15    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+215;
	public static final int QUERY_16    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+216;
	public static final int QUERY_17    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+217;
	public static final int QUERY_18    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+218;
	public static final int QUERY_19    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+219;
	public static final int QUERY_20    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+220;
	
	public static final int EVENT_1     = RESERVED_POD_REQUEST_METHOD_TAG_MIN+301;
	public static final int EVENT_2     = RESERVED_POD_REQUEST_METHOD_TAG_MIN+302;
	public static final int EVENT_3     = RESERVED_POD_REQUEST_METHOD_TAG_MIN+303;
	public static final int EVENT_4     = RESERVED_POD_REQUEST_METHOD_TAG_MIN+304;
	public static final int EVENT_5     = RESERVED_POD_REQUEST_METHOD_TAG_MIN+305;
	public static final int EVENT_6     = RESERVED_POD_REQUEST_METHOD_TAG_MIN+306;
	public static final int EVENT_7     = RESERVED_POD_REQUEST_METHOD_TAG_MIN+307;
	public static final int EVENT_8     = RESERVED_POD_REQUEST_METHOD_TAG_MIN+308;
	public static final int EVENT_9     = RESERVED_POD_REQUEST_METHOD_TAG_MIN+309;
	public static final int EVENT_10    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+310;
	
	public static final int EVENT_11    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+311;
	public static final int EVENT_12    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+312;
	public static final int EVENT_13    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+313;
	public static final int EVENT_14    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+314;
	public static final int EVENT_15    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+315;
	public static final int EVENT_16    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+316;
	public static final int EVENT_17    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+317;
	public static final int EVENT_18    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+318;
	public static final int EVENT_19    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+319;
	public static final int EVENT_20    = RESERVED_POD_REQUEST_METHOD_TAG_MIN+320;
}