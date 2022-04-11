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
package io.actor4j.core.runtime.pods;

import java.io.File;
import java.util.Map;

import io.actor4j.core.pods.PodConfiguration;
import io.actor4j.core.pods.PodFactory;

public interface PodReplicationController {
	public Map<String, PodReplicationTuple> getPodReplicationMap();
	
	public void deployPods(File jarFile, PodConfiguration podConfiguration);
	public void deployPods(PodFactory factory, PodConfiguration podConfiguration);
	
	public void undeployPod(String domain, String shardId, int instances);
	public void undeployPods(String domain);
	
	public void updatePods(File jarFile, PodConfiguration podConfiguration);
	public void updatePods(String domain, PodFactory factory, PodConfiguration podConfiguration);
	
	public void increasePods(String domain, String shardId);
	public void increasePods(String domain, String shardId, int instances);
	
	public void decreasePods(String domain, String shardId);
	public void decreasePods(String domain, String shardId, int instances);
}
