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

import org.junit.Test;

import cloud.actor4j.core.utils.CacheLRU;
import cloud.actor4j.core.utils.CacheLRUWithGC;
import cloud.actor4j.core.utils.DefaultCache;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.Map.Entry;

public class CacheFeature {
	@Test
	public void test_cache_lru_with_gc__get_put_resize() {
		CacheLRUWithGC<String, String> cache = new CacheLRUWithGC<>(5);
		
		String[][] data = { 
				{"A", "AA"}, 
				{"B", "BB"},
				{"C", "CC"},
				{"D", "DD"},
				{"E", "EE"},
				{"F", "FF"},
				{"G", "GG"}
		};
		
		cache.put(data[0][0], data[0][1]);
		cache.put(data[1][0], data[1][1]);
		cache.put(data[2][0], data[2][1]);
		cache.put(data[3][0], data[3][1]);
		cache.put(data[4][0], data[4][1]);
		
		for (int i=0; i<data.length-2; i++)
			assertEquals(data[i][1], cache.get(data[i][0]));
		
		assertTrue(cache.getMap().size()==5);
		assertTrue(cache.getLru().size()==5);
		cache.put(data[5][0], data[5][1]);
		cache.put(data[6][0], data[6][1]);
		assertTrue(cache.getMap().size()==5);
		assertTrue(cache.getLru().size()==5);
		
		int i=2;
		for (String key : cache.getMap().keySet()) {
			assertEquals(data[i++][0], key);
		}
		
		cache.get(data[5][0]);
		cache.get(data[4][0]);
		
		Iterator<Entry<Long, String>> iterator = cache.getLru().entrySet().iterator();
		assertEquals(data[2][0], iterator.next().getValue());
		assertEquals(data[3][0], iterator.next().getValue());
		assertEquals(data[6][0], iterator.next().getValue());
		assertEquals(data[5][0], iterator.next().getValue());
		assertEquals(data[4][0], iterator.next().getValue());
		/*
		String nextToLast = null;
		String last = null;
		Iterator<Entry<Long, String>> iterator = cache.getLru().entrySet().iterator();
		while (iterator.hasNext()) {
			nextToLast = last;
			last = iterator.next().getValue();
		}
		assertEquals(data[5][0], nextToLast);
		assertEquals(data[4][0], last);
		*/
	}
	
	@Test
	public void test_cache_lru__get_put_resize() {
		CacheLRU<String, String> cache = new CacheLRU<>(5);
		
		String[][] data = { 
				{"A", "AA"}, 
				{"B", "BB"},
				{"C", "CC"},
				{"D", "DD"},
				{"E", "EE"},
				{"F", "FF"},
				{"G", "GG"}
		};
		
		cache.put(data[0][0], data[0][1]);
		cache.put(data[1][0], data[1][1]);
		cache.put(data[2][0], data[2][1]);
		cache.put(data[3][0], data[3][1]);
		cache.put(data[4][0], data[4][1]);
		
		for (int i=0; i<data.length-2; i++)
			assertEquals(data[i][1], cache.get(data[i][0]));
		
		assertTrue(cache.getMap().size()==5);
		assertTrue(cache.getLru().size()==5);
		cache.put(data[5][0], data[5][1]);
		cache.put(data[6][0], data[6][1]);
		assertTrue(cache.getMap().size()==5);
		assertTrue(cache.getLru().size()==5);
		
		int i=2;
		for (String key : cache.getMap().keySet()) {
			assertEquals(data[i++][0], key);
		}
		
		cache.get(data[5][0]);
		cache.get(data[4][0]);

		Iterator<String> iterator = cache.getLru().iterator();
		assertEquals(data[2][0], iterator.next());
		assertEquals(data[3][0], iterator.next());
		assertEquals(data[6][0], iterator.next());
		assertEquals(data[5][0], iterator.next());
		assertEquals(data[4][0], iterator.next());
	}
	
	@Test
	public void test_cache_default__get_put() {
		DefaultCache<String, String> cache = new DefaultCache<>();
		
		String[][] data = { 
				{"A", "AA"}, 
				{"B", "BB"},
				{"C", "CC"},
				{"D", "DD"},
				{"E", "EE"},
				{"F", "FF"},
				{"G", "GG"}
		};
		
		cache.put(data[0][0], data[0][1]);
		cache.put(data[1][0], data[1][1]);
		cache.put(data[2][0], data[2][1]);
		cache.put(data[3][0], data[3][1]);
		cache.put(data[4][0], data[4][1]);
		
		for (int i=0; i<data.length-2; i++)
			assertEquals(data[i][1], cache.get(data[i][0]));
		
		assertTrue(cache.getMap().size()==5);
		cache.put(data[5][0], data[5][1]);
		cache.put(data[6][0], data[6][1]);
		assertTrue(cache.getMap().size()==7);
		
		int i=0;
		for (String key : cache.getMap().keySet()) {
			assertEquals(data[i++][0], key);
		}
	}
}
