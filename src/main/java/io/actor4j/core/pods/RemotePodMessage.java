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

import io.actor4j.core.utils.Shareable;

public final class RemotePodMessage implements Shareable {
	private final RemotePodMessageDTO remotePodMessageDTO; 
	private final String replyAddress; 
	private final Object user;
	
	public RemotePodMessage(RemotePodMessageDTO remotePodMessageDTO, String replyAddress, Object user) {
		super();
		this.remotePodMessageDTO = remotePodMessageDTO;
		this.replyAddress = replyAddress;
		this.user = user;
	}
	
	public RemotePodMessageDTO remotePodMessageDTO() {
		return remotePodMessageDTO;
	}
	
	public String replyAddress() {
		return replyAddress;
	}
	
	public Object user() {
		return user;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((remotePodMessageDTO == null) ? 0 : remotePodMessageDTO.hashCode());
		result = prime * result + ((replyAddress == null) ? 0 : replyAddress.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
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
		RemotePodMessage other = (RemotePodMessage) obj;
		if (remotePodMessageDTO == null) {
			if (other.remotePodMessageDTO != null)
				return false;
		} else if (!remotePodMessageDTO.equals(other.remotePodMessageDTO))
			return false;
		if (replyAddress == null) {
			if (other.replyAddress != null)
				return false;
		} else if (!replyAddress.equals(other.replyAddress))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RemotePodMessage [remotePodMessageDTO=" + remotePodMessageDTO + ", replyAddress=" + replyAddress
				+ ", user=" + user + "]";
	}
}
