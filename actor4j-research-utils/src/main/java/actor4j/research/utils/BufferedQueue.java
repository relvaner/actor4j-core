package actor4j.research.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.queue.CircularFifoQueue;

public class BufferedQueue<E> implements Queue<E> {
	protected Queue<E> bufferQueue;
	protected Queue<E> queue;
	
	protected AtomicInteger lock;
	
	public BufferedQueue(int capacity) {
		super();
		
		bufferQueue = new CircularFifoQueue<>(capacity);
		queue       = new CircularFifoQueue<>(capacity);
		
		lock = new AtomicInteger();
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterator<E> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean add(E e) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * 
	 */
	@Override
	public boolean offer(E e) {
		boolean result = false;
		while (lock.compareAndSet(0, 1));
		result = bufferQueue.offer(e);
		lock.set(0);
		return result;
	}

	@Override
	public E remove() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public E poll() {
		E result = queue.poll();
		if (result==null) {
			while (lock.compareAndSet(0, 1));
			E data = null;
			for (int i=0; (data=bufferQueue.poll())!=null && i<50000; i++)
				queue.offer(data);
			result = queue.poll();
			lock.set(0);
		}
		return result;
	}

	@Override
	public E element() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public E peek() {
		// TODO Auto-generated method stub
		return null;
	}
}
