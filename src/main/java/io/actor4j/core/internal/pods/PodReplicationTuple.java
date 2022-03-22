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
package io.actor4j.core.internal.pods;

import io.actor4j.core.pods.PodConfiguration;

public final class PodReplicationTuple {
	private final PodConfiguration podConfiguration; 
	private final PodSystemConfiguration podSystemConfiguration; 
	private final String jarFileName;
	
	public PodReplicationTuple(PodConfiguration podConfiguration, PodSystemConfiguration podSystemConfiguration,
			String jarFileName) {
		super();
		this.podConfiguration = podConfiguration;
		this.podSystemConfiguration = podSystemConfiguration;
		this.jarFileName = jarFileName;
	}

	public PodReplicationTuple(PodConfiguration podConfiguration, PodSystemConfiguration podSystemConfiguration) {
		this(podConfiguration, podSystemConfiguration, null);
	}
	
	public boolean hasJarFile() {
		return jarFileName!=null;
	}
	
	public PodConfiguration podConfiguration() {
		return podConfiguration;
	}
	
	public PodSystemConfiguration podSystemConfiguration() {
		return podSystemConfiguration;
	}
	
	public String jarFileName() {
		return jarFileName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((jarFileName == null) ? 0 : jarFileName.hashCode());
		result = prime * result + ((podConfiguration == null) ? 0 : podConfiguration.hashCode());
		result = prime * result + ((podSystemConfiguration == null) ? 0 : podSystemConfiguration.hashCode());
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
		PodReplicationTuple other = (PodReplicationTuple) obj;
		if (jarFileName == null) {
			if (other.jarFileName != null)
				return false;
		} else if (!jarFileName.equals(other.jarFileName))
			return false;
		if (podConfiguration == null) {
			if (other.podConfiguration != null)
				return false;
		} else if (!podConfiguration.equals(other.podConfiguration))
			return false;
		if (podSystemConfiguration == null) {
			if (other.podSystemConfiguration != null)
				return false;
		} else if (!podSystemConfiguration.equals(other.podSystemConfiguration))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PodReplicationTuple [podConfiguration=" + podConfiguration + ", podSystemConfiguration="
				+ podSystemConfiguration + ", jarFileName=" + jarFileName + "]";
	}
}
