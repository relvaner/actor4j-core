package actor4j.core.features;

import org.junit.Test;

import actor4j.core.utils.CacheLRUWithGC;

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
}
