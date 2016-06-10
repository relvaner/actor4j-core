package actor4j.utils;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CacheLRUWithGC<K, E> implements Cache<K, E>  {
	protected Map<K, E> map;
	protected Deque<K> lru;
	protected Map<K, Long> timestampMap;
	
	protected int size;
	
	public CacheLRUWithGC(int size) {
		map = new HashMap<>(size);
		lru = new ArrayDeque<>(size);
		timestampMap = new HashMap<>(size); 
		
		this.size = size;
	}
	
	@Override
	public E get(K key) {
		E result = map.get(key);
		
		if (result!=null) {
			lru.remove(key);
			lru.addLast(key);
			timestampMap.put(key, System.currentTimeMillis());
		}
		
		return result;
	}
	
	@Override
	public E put(K key, E value) {
		E result = map.put(key, value);
		
		if (result==null) {
			resize();
			lru.addLast(key);
		}
		else {
			lru.remove(key);
			lru.addLast(key);
		}
		timestampMap.put(key, System.currentTimeMillis());
		
		return result;
	}
	
	protected void resize() {
		if (map.size()>size) {
			map.remove(lru.getFirst());
			timestampMap.remove(lru.getFirst());
			lru.removeFirst();
		}
	}
	
	@Override
	public void gc(long maxTime) {
		long currentTime = System.currentTimeMillis();
		
		Iterator<K> iterator = lru.iterator();
		while (iterator.hasNext()) {
			K key = iterator.next();
			if (currentTime-timestampMap.get(key)>maxTime) {
				map.remove(key);
				iterator.remove();
				timestampMap.remove(key);
			}
		}
	}

	@Override
	public String toString() {
		return "CacheLRUWithGC [map=" + map + ", lru=" + lru + ", timestampMap=" + timestampMap + ", size=" + size
				+ "]";
	}
}
