/*
 * Copyright (c) 2015-2026, David A. Bauer. All rights reserved.
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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

// hybrid runtime with all actors pooled except thread-bound groups: must behave like the classic runtime
@RunWith(Suite.class)
@Suite.SuiteClasses(AllFeaturesTest.class)
public class TckPooledTest {
	@BeforeClass
	public static void beforeClass() {
		System.setProperty("actor4j.runtime", "hybrid-pooled"); // like classic: watchdog off, thread-bound group tests skipped
		System.setProperty("actor4j.hybrid.placement", "pooled");
	}

	@AfterClass
	public static void afterClass() {
		System.clearProperty("actor4j.hybrid.placement");
	}
}
