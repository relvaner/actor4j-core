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

package io.actor4j.core.pods;

public final class RemotePodMessageDTO {
	private final Object payload;
	private final int tag;
	private final String alias;
	private final Object auth;
	private final boolean reply;

	public RemotePodMessageDTO(Object payload, int tag, String alias, Object auth, boolean reply) {
		super();
		this.payload = payload;
		this.tag = tag;
		this.alias = alias;
		this.auth = auth;
		this.reply = reply;
	}

	public RemotePodMessageDTO(Object payload, int tag, String alias, boolean reply) {
		this(payload, tag, alias, null, reply);
	}

	public Object payload() {
		return payload;
	}
	
	public int tag() {
		return tag;
	}
	
	public String alias() {
		return alias;
	}
	
	public Object auth() {
		return auth;
	}
	
	public boolean reply() {
		return reply;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alias == null) ? 0 : alias.hashCode());
		result = prime * result + ((auth == null) ? 0 : auth.hashCode());
		result = prime * result + ((payload == null) ? 0 : payload.hashCode());
		result = prime * result + (reply ? 1231 : 1237);
		result = prime * result + tag;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RemotePodMessageDTO other = (RemotePodMessageDTO) obj;
		if (alias == null) {
			if (other.alias != null)
				return false;
		} else if (!alias.equals(other.alias))
			return false;
		if (auth == null) {
			if (other.auth != null)
				return false;
		} else if (!auth.equals(other.auth))
			return false;
		if (payload == null) {
			if (other.payload != null)
				return false;
		} else if (!payload.equals(other.payload))
			return false;
		if (reply != other.reply)
			return false;
		if (tag != other.tag)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "RemotePodMessageDTO [payload=" + payload + ", tag=" + tag + ", alias=" + alias + ", auth=" + auth
				+ ", reply=" + reply + "]";
	}
}
