package actor4j.research;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.queue.CircularFifoQueue;

public class BufferedQueue2<E> implements Queue<E> {
	protected Queue<E> queue1;
	protected Queue<E> queue2;
	
	protected AtomicInteger lock;
	
	public BufferedQueue2(int capacity) {
		super();
		
		queue1 = new CircularFifoQueue<>(capacity);
		queue2 = new CircularFifoQueue<>(capacity);
		
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
		if (lock.get()==0) {
			result = queue1.offer(e);
			lock.set(1);
		}
		else {
			result = queue2.offer(e);
			lock.set(0);
		}
		return result;
	}

	@Override
	public E remove() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public E poll() {
		E result = null;
		if (lock.get()==0) {
			result = queue2.poll();
			lock.set(1);
		}
		else {
			result = queue1.poll();
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
