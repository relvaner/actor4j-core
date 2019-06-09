/*
 * Copyright (c) 2015-2018, David A. Bauer. All rights reserved.
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
package cloud.actor4j.core.features;

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Test;

import io.actor4j.core.utils.ActorOptional;

public class OptionalFeature {
	@Test
	public void test() {
		Optional<Integer> optional1 = null;
		
		optional1 = Optional.ofNullable(0);
		assertTrue(optional1.isPresent());
		assertEquals(0, (int)optional1.get());
		
		optional1 = Optional.ofNullable(null);
		assertFalse(optional1.isPresent());
		
		/*----------------------------------*/
		
		ActorOptional<Integer> optional2 = null;
		
		optional2 = ActorOptional.of(0);
		assertTrue(optional2.isDone());
		assertTrue(optional2.isPresent());
		assertEquals(0, (int)optional2.get());
		
		optional2 = ActorOptional.of(null);
		assertTrue(optional2.isDone());
		assertFalse(optional2.isPresent());
		assertNull(optional2.get());
		
		optional2 = ActorOptional.none();
		assertFalse(optional2.isDone());
		assertFalse(optional2.isPresent());
		assertNull(optional2.get());
	}
}
