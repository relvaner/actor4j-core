package actor4j.core.features;

import org.junit.Test;

import actor4j.core.utils.CacheLRUWithGC;

import static org.junit.Assert.*;

public class CacheFeature {
	@Test
	public void test_cache_lru_with_gc() {
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
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		cache.put(data[1][0], data[1][1]);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		cache.put(data[2][0], data[2][1]);
		cache.put(data[3][0], data[3][1]);
		cache.put(data[4][0], data[4][1]);
		
		assertTrue(cache.getMap().size()==5);
		cache.put(data[5][0], data[5][1]);
		cache.put(data[6][0], data[6][1]);
		assertTrue(cache.getMap().size()==5);
		
		int i=2;
		for (String key : cache.getMap().keySet()) {
			assertEquals(data[i++][0], key);
		}
	}
}
