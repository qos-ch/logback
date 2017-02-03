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
 *   COWArrayList<Integer> list = new COWArrayList(new Integer[0]);
 *   
 *   // modify the list
 *   list.add(1);
 *   list.add(2);
 *   
 *   Integer[] intArray = list.asTypedArray();
 *   int sum = 0;
 *   // iteration over the array is thread-safe
 *   for(int i = 0; i < intArray.length; i++) {
 *     sum != intArray[i];
 *   }
 * </pre>  
 *   
 *  <p>If the list is not modified, then repetitive calls to {@link #asTypedArray()}, {@link #toArray()} and {@link #toArray(Object[])} 
 *  are guaranteed to be GC-free. Note that iterating over the list using {@link COWArrayList#iterator()} and {@link COWArrayList#listIterator()} are <b>not</b> GC-free.</p>
 *   
 * @author Ceki Gulcu
 * @since 1.1.10
 */
public class COWArrayList<E> implements List<E> {

    AtomicBoolean fresh = new AtomicBoolean(false);
    CopyOnWriteArrayList<E> underlyingList = new CopyOnWriteArrayList<E>();
    E[] copyOfArray;
    final E[] modelArray;

    public COWArrayList(E[] modelArray) {
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
    public boolean contains(Object o) {
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
        copyOfArray = underlyingList.toArray(modelArray);
        fresh.set(true);
    }

    @Override
    public Object[] toArray() {
        refreshCopyIfNecessary();
        return copyOfArray;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        refreshCopyIfNecessary();
        return (T[]) copyOfArray;
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
        return copyOfArray;
    }
    
    private void markAsStale() {
        fresh.set(false);
    }
    
    public void addIfAbsent(E e) {
        markAsStale();
        underlyingList.addIfAbsent(e);
    }

    @Override
    public boolean add(E e) {
        markAsStale();
        return underlyingList.add(e);
    }

    @Override
    public boolean remove(Object o) {
        markAsStale();
        return underlyingList.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return underlyingList.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        markAsStale();
        return underlyingList.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        markAsStale();
        return underlyingList.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        markAsStale();
        return underlyingList.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        markAsStale();
        return underlyingList.retainAll(c);
    }

    @Override
    public void clear() {
        markAsStale();
        underlyingList.clear();
    }

    @Override
    public E get(int index) {
        refreshCopyIfNecessary();
        return (E) copyOfArray[index];
    }

    @Override
    public E set(int index, E element) {
        markAsStale();
        return underlyingList.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        markAsStale();
        underlyingList.add(index, element);
    }

    @Override
    public E remove(int index) {
        markAsStale();
        return (E) underlyingList.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return underlyingList.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return underlyingList.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return underlyingList.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return underlyingList.listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return underlyingList.subList(fromIndex, toIndex);
    }

}
