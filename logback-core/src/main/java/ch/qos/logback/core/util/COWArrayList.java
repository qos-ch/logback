package ch.qos.logback.core.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A GC-free lock-free thread-safe implementation of the {@link List} interface for use cases where iterations over the list vastly out-number modifications on the list.
 *
 * <p>Underneath, it wraps an instance of {@link CopyOnWriteArrayList} and exposes a copy of the array used by that instance.
 *
 * <p>Typical use:</p>
 *
 * <pre>
 *   COWArrayList&lt;Integer&gt; list = new COWArrayList(new Integer[0]);
 *
 *   // modify the list
 *   list.add(1);
 *   list.add(2);
 *
 *   Integer[] intArray = list.asTypedArray();
 *   int sum = 0;
 *   // iteration over the array is thread-safe
 *   for(int i = 0; i &lt; intArray.length; i++) {
 *     sum != intArray[i];
 *   }
 * </pre>
 *
 *  <p>If the list is not modified, then repetitive calls to {@link #asTypedArray()}, {@link #toArray()} and
 *  {@link #toArray(Object[])} are guaranteed to be GC-free. Note that iterating over the list using
 *  {@link COWArrayList#iterator()} and {@link COWArrayList#listIterator()} are <b>not</b> GC-free.</p>
 *
 * @author Ceki Gulcu
 * @since 1.1.10
 */
public class COWArrayList<E> implements List<E> {

	// Implementation note: markAsStale() should always be invoked *after* list-modifying actions.
	// If not, readers might get a stale array until the next write. The potential problem is nicely
	// explained by Rob Eden. See https://github.com/qos-ch/logback/commit/32a2047a1adfc#commitcomment-20791176

	AtomicBoolean fresh = new AtomicBoolean(false);
	CopyOnWriteArrayList<E> underlyingList = new CopyOnWriteArrayList<>();
	E[] ourCopy;
	final E[] modelArray;

	public COWArrayList(final E[] modelArray) {
		this.modelArray = modelArray;
	}

	@Override
	public int size() {
		return underlyingList.size();
	}

	@Override
	public boolean isEmpty() {
		return underlyingList.isEmpty();
	}

	@Override
	public boolean contains(final Object o) {
		return underlyingList.contains(o);
	}

	@Override
	public Iterator<E> iterator() {
		return underlyingList.iterator();
	}

	private void refreshCopyIfNecessary() {
		if (!isFresh()) {
			refreshCopy();
		}
	}

	private boolean isFresh() {
		return fresh.get();
	}

	private void refreshCopy() {
		ourCopy = underlyingList.toArray(modelArray);
		fresh.set(true);
	}

	@Override
	public Object[] toArray() {
		refreshCopyIfNecessary();
		return ourCopy;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(final T[] a) {
		refreshCopyIfNecessary();
		return (T[]) ourCopy;
	}

	/**
	 * Return an array of type E[]. The returned array is intended to be iterated over.
	 * If the list is modified, subsequent calls to this method will return different/modified
	 * array instances.
	 *
	 * @return
	 */
	public E[] asTypedArray() {
		refreshCopyIfNecessary();
		return ourCopy;
	}

	private void markAsStale() {
		fresh.set(false);
	}

	public void addIfAbsent(final E e) {
		underlyingList.addIfAbsent(e);
		markAsStale();
	}

	@Override
	public boolean add(final E e) {
		final boolean result = underlyingList.add(e);
		markAsStale();
		return result;
	}

	@Override
	public boolean remove(final Object o) {
		final boolean result = underlyingList.remove(o);
		markAsStale();
		return result;
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		return underlyingList.containsAll(c);
	}

	@Override
	public boolean addAll(final Collection<? extends E> c) {
		markAsStale();
		return underlyingList.addAll(c);
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends E> col) {
		markAsStale();
		return underlyingList.addAll(index, col);
	}

	@Override
	public boolean removeAll(final Collection<?> col) {
		markAsStale();
		return underlyingList.removeAll(col);
	}

	@Override
	public boolean retainAll(final Collection<?> col) {
		markAsStale();
		return underlyingList.retainAll(col);
	}

	@Override
	public void clear() {
		markAsStale();
		underlyingList.clear();
	}

	@Override
	public E get(final int index) {
		refreshCopyIfNecessary();
		return ourCopy[index];
	}

	@Override
	public E set(final int index, final E element) {
		markAsStale();
		return underlyingList.set(index, element);
	}

	@Override
	public void add(final int index, final E element) {
		markAsStale();
		underlyingList.add(index, element);
	}

	@Override
	public E remove(final int index) {
		markAsStale();
		return underlyingList.remove(index);
	}

	@Override
	public int indexOf(final Object o) {
		return underlyingList.indexOf(o);
	}

	@Override
	public int lastIndexOf(final Object o) {
		return underlyingList.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		return underlyingList.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(final int index) {
		return underlyingList.listIterator(index);
	}

	@Override
	public List<E> subList(final int fromIndex, final int toIndex) {
		return underlyingList.subList(fromIndex, toIndex);
	}

}
