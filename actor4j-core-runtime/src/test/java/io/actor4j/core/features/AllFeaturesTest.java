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
package io.actor4j.core.features;

import org.junit.runners.Suite;

import io.actor4j.core.internal.DefaultActorGlobalSettings;

import static io.actor4j.core.logging.ActorLogger.*;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	ConfigFeature.class,
	
	ActorFeature.class,
	LifeCycleFeature.class,
	BehaviourFeature.class,
	AwaitFeature.class,
	MatcherFeature.class,
	HandlerFeature.class,
	FailsafeFeature.class,
	WatchdogFeature.class,
	/*UnhandledFeature.class,*/
	
	EmbeddedActorFeature.class,
	PseudoActorFeature.class,
	ConcurrentPseudoActorFeature.class,
	StatelessActorFeature.class,
	PrimarySecondaryActorFeature.class,
	ResourceActorFeature.class,
	
	ActorGroupMemberFeature.class,
	CacheFeature.class,
	CacheHandlerFeature.class,
	
	ServiceDiscoveyFeature.class,
	
	AskPatternFeature.class,
	CommPatternFeature.class,
	OptionalFeature.class,
	
	PodFeature.class
})
public class AllFeaturesTest {
	@BeforeClass
	public static void beforeClass() {
		DefaultActorGlobalSettings.override();
		
		systemLogger().setLevel(ERROR);
		logger().setLevel(ERROR);
	}
}
