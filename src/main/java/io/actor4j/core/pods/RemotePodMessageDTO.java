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

package io.actor4j.core.pods;

public class RemotePodMessageDTO {
	public final Object payload;
	public final int tag;
	public final String alias;
	
	public final Object auth;
	
	public final boolean reply;
	
	public RemotePodMessageDTO() {
		super();
		this.payload = null;
		this.tag = 0;
		this.alias = null;
		this.auth = null;
		this.reply = false;
	}
	
	public RemotePodMessageDTO(Object payload, int tag, String alias, Object auth, boolean reply) {
		super();
		this.payload = payload;
		this.tag = tag;
		this.alias = alias;
		this.auth = auth;
		this.reply = reply;
	}

	@Override
	public String toString() {
		return "RemotePodMessageDTO [payload=" + payload + ", tag=" + tag + ", alias=" + alias + ", auth="
				+ auth + ", reply=" + reply + "]";
	}
}
