package actor4j.utils;

public interface Cache<K, E> {
	public E get(K key);
	public E put(K key, E value);
	
	public void gc(long maxTime);
}
