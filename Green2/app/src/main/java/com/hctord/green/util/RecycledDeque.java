package com.hctord.green.util;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * Queue that, instead of nulling out objects that it doesn't use, retains their space so that
 * if the space were to be filled again, instead of going through the trouble of instantiating
 * objects it simply recycles the already existant elements.
 */
public class RecycledDeque<E> implements Queue<E> {

    private class Iterator implements java.util.Iterator<E> {
        private int index = 0;

        /**
         * Returns true if there is at least one more element, false otherwise.
         *
         * @see #next
         */
        @Override
        public boolean hasNext() {
            return index < used;
        }

        /**
         * Returns the next object and advances the iterator.
         *
         * @return the next object.
         * @throws NoSuchElementException if there are no more elements.
         * @see #hasNext
         */
        @Override
        public E next() {
            if (index <= used)
                return (E)items[index++];
            else
                throw new NoSuchElementException();
        }

        /**
         * Removes the last object returned by {@code next} from the collection.
         * This method can only be called once between each call to {@code next}.
         *
         * @throws UnsupportedOperationException if removing is not supported by the collection being
         *                                       iterated.
         * @throws IllegalStateException         if {@code next} has not been called, or {@code remove} has
         *                                       already been called after the last call to {@code next}.
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private Object[] items;
    private int used;
    private int interval;
    private Recycler<E> recycler;
    private Comparer<E> comparer;
    private Class<E> eClass;

    public RecycledDeque(Recycler<E> recycler, Comparer<E> comparer, Class<E> eClass) {
        this(recycler, comparer, eClass, 4);
    }

    public RecycledDeque(Recycler<E> recycler, Comparer<E> comparer, Class<E> eClass,
                         int initialCapacity) {
        this(recycler, comparer, eClass, initialCapacity, 0);
    }

    public RecycledDeque(Recycler<E> recycler, Comparer<E> comparer, Class<E> eClass,
                         int initialCapacity, int interval) {
        this.recycler = recycler;
        used = 0;
        items = new Object[initialCapacity];
        this.interval = interval;
        this.eClass = eClass;
        this.comparer = comparer;
        // Test creating new instance
        try {
            eClass.newInstance();
        }
        catch (InstantiationException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Type has no public default constructor or " +
                    "error occurred during instantiation");
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Type has no public default constructor");
        }
    }

    public void addAndRecycle(Object...params) {
        if (used == items.length) {
            Object[] newItems = new Object[interval == 0 ? used * 2 : used + interval];
            System.arraycopy(items, 0, newItems, 0, used);
            items = newItems;
        }
        if (items[used] == null) {
            try {
                items[used] = eClass.newInstance();
            }
            catch (InstantiationException e) {
                e.printStackTrace();
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        recycler.recycle((E)items[used++], params);
    }

    /**
     * Inserts the specified element into this queue if it is possible to do so
     * immediately without violating capacity restrictions, returning
     * <tt>true</tt> upon success and throwing an <tt>IllegalStateException</tt>
     * if no space is currently available.
     *
     * @param e the element to add
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     * @throws IllegalStateException    if the element cannot be added at this
     *                                  time due to capacity restrictions
     * @throws ClassCastException       if the class of the specified element
     *                                  prevents it from being added to this queue
     * @throws NullPointerException     if the specified element is null and
     *                                  this queue does not permit null elements
     * @throws IllegalArgumentException if some property of this element
     *                                  prevents it from being added to this queue
     */
    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException("Use addAndRecycle instead");
    }

    /**
     * Attempts to add all of the objects contained in {@code Collection}
     * to the contents of this {@code Collection} (optional). If the passed {@code Collection}
     * is changed during the process of adding elements to this {@code Collection}, the
     * behavior is not defined.
     *
     * @param collection the {@code Collection} of objects.
     * @return {@code true} if this {@code Collection} is modified, {@code false}
     * otherwise.
     * @throws UnsupportedOperationException if adding to this {@code Collection} is not supported.
     * @throws ClassCastException            if the class of an object is inappropriate for this
     *                                       {@code Collection}.
     * @throws IllegalArgumentException      if an object cannot be added to this {@code Collection}.
     * @throws NullPointerException          if {@code collection} is {@code null}, or if it
     *                                       contains {@code null} elements and this {@code Collection} does
     *                                       not support such elements.
     */
    @Override
    public boolean addAll(Collection<? extends E> collection) {
        throw new UnsupportedOperationException("Adding all of a collection not supported");
    }

    /**
     * Removes all elements from this {@code Collection}, leaving it empty (optional).
     *
     * @throws UnsupportedOperationException if removing from this {@code Collection} is not supported.
     * @see #isEmpty
     * @see #size
     */
    @Override
    public void clear() {
        used = 0;
    }

    /**
     * Tests whether this {@code Collection} contains the specified object. Returns
     * {@code true} if and only if at least one element {@code elem} in this
     * {@code Collection} meets following requirement:
     * {@code (object==null ? elem==null : object.equals(elem))}.
     *
     * @param object the object to search for.
     * @return {@code true} if object is an element of this {@code Collection},
     * {@code false} otherwise.
     * @throws ClassCastException   if the object to look for isn't of the correct
     *                              type.
     * @throws NullPointerException if the object to look for is {@code null} and this
     *                              {@code Collection} doesn't support {@code null} elements.
     */
    @Override
    public boolean contains(Object object) {
        for (int i = 0; i < used; i++) {
            if (items[i] == object ||
                comparer.compare((E)items[i], (E)object))
                return true;
        }
        return false;
    }

    /**
     * Tests whether this {@code Collection} contains all objects contained in the
     * specified {@code Collection}. If an element {@code elem} is contained several
     * times in the specified {@code Collection}, the method returns {@code true} even
     * if {@code elem} is contained only once in this {@code Collection}.
     *
     * @param collection the collection of objects.
     * @return {@code true} if all objects in the specified {@code Collection} are
     * elements of this {@code Collection}, {@code false} otherwise.
     * @throws ClassCastException   if one or more elements of {@code collection} isn't of the
     *                              correct type.
     * @throws NullPointerException if {@code collection} contains at least one {@code null}
     *                              element and this {@code Collection} doesn't support {@code null}
     *                              elements.
     * @throws NullPointerException if {@code collection} is {@code null}.
     */
    @Override
    public boolean containsAll(Collection<?> collection) {
        E item;
        for (Object obj : collection) {
            try {
                item = (E)obj;
            }
            catch (ClassCastException e) {
                return false;
            }
            for (int i = 0; i < used; i++) {
                if (!(items[i] == item ||
                    comparer.compare((E)items[i], item)))
                    return false;
            }
        }
        return true;
    }

    /**
     * Returns if this {@code Collection} contains no elements.
     *
     * @return {@code true} if this {@code Collection} has no elements, {@code false}
     * otherwise.
     * @see #size
     */
    @Override
    public boolean isEmpty() {
        return used == 0;
    }

    /**
     * Returns an instance of {@link java.util.Iterator} that may be used to access the
     * objects contained by this {@code Collection}. The order in which the elements are
     * returned by the iterator is not defined. Only if the instance of the
     * {@code Collection} has a defined order the elements are returned in that order.
     *
     * @return an iterator for accessing the {@code Collection} contents.
     */
    @Override
    public java.util.Iterator<E> iterator() {
        return new Iterator();
    }

    /**
     * Removes one instance of the specified object from this {@code Collection} if one
     * is contained (optional). The element {@code elem} that is removed
     * complies with {@code (object==null ? elem==null : object.equals(elem)}.
     *
     * @param object the object to remove.
     * @return {@code true} if this {@code Collection} is modified, {@code false}
     * otherwise.
     * @throws UnsupportedOperationException if removing from this {@code Collection} is not supported.
     * @throws ClassCastException            if the object passed is not of the correct type.
     * @throws NullPointerException          if {@code object} is {@code null} and this {@code Collection}
     *                                       doesn't support {@code null} elements.
     */
    @Override
    public boolean remove(Object object) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes all occurrences in this {@code Collection} of each object in the
     * specified {@code Collection} (optional). After this method returns none of the
     * elements in the passed {@code Collection} can be found in this {@code Collection}
     * anymore.
     *
     * @param collection the collection of objects to remove.
     * @return {@code true} if this {@code Collection} is modified, {@code false}
     * otherwise.
     * @throws UnsupportedOperationException if removing from this {@code Collection} is not supported.
     * @throws ClassCastException            if one or more elements of {@code collection}
     *                                       isn't of the correct type.
     * @throws NullPointerException          if {@code collection} contains at least one
     *                                       {@code null} element and this {@code Collection} doesn't support
     *                                       {@code null} elements.
     */
    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes all objects from this {@code Collection} that are not also found in the
     * {@code Collection} passed (optional). After this method returns this {@code Collection}
     * will only contain elements that also can be found in the {@code Collection}
     * passed to this method.
     *
     * @param collection the collection of objects to retain.
     * @return {@code true} if this {@code Collection} is modified, {@code false}
     * otherwise.
     * @throws UnsupportedOperationException if removing from this {@code Collection} is not supported.
     * @throws ClassCastException            if one or more elements of {@code collection}
     *                                       isn't of the correct type.
     * @throws NullPointerException          if {@code collection} contains at least one
     *                                       {@code null} element and this {@code Collection} doesn't support
     *                                       {@code null} elements.
     * @throws NullPointerException          if {@code collection} is {@code null}.
     */
    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a count of how many objects this {@code Collection} contains.
     *
     * @return how many objects this {@code Collection} contains, or Integer.MAX_VALUE
     * if there are more than Integer.MAX_VALUE elements in this
     * {@code Collection}.
     */
    @Override
    public int size() {
        return used;
    }

    /**
     * Returns a new array containing all elements contained in this {@code Collection}.
     * <p/>
     * If the implementation has ordered elements it will return the element
     * array in the same order as an iterator would return them.
     * <p/>
     * The array returned does not reflect any changes of the {@code Collection}. A new
     * array is created even if the underlying data structure is already an
     * array.
     *
     * @return an array of the elements from this {@code Collection}.
     */
    @Override
    public Object[] toArray() {
        return items.clone();
    }

    /**
     * Returns an array containing all elements contained in this {@code Collection}. If
     * the specified array is large enough to hold the elements, the specified
     * array is used, otherwise an array of the same type is created. If the
     * specified array is used and is larger than this {@code Collection}, the array
     * element following the {@code Collection} elements is set to null.
     * <p/>
     * If the implementation has ordered elements it will return the element
     * array in the same order as an iterator would return them.
     * <p/>
     * {@code toArray(new Object[0])} behaves exactly the same way as
     * {@code toArray()} does.
     *
     * @param array the array.
     * @return an array of the elements from this {@code Collection}.
     * @throws ArrayStoreException if the type of an element in this {@code Collection} cannot be
     *                             stored in the type of the specified array.
     */
    @Override
    public <T> T[] toArray(T[] array) {
        return null;
    }

    /**
     * Inserts the specified element into this queue if it is possible to do
     * so immediately without violating capacity restrictions.
     * When using a capacity-restricted queue, this method is generally
     * preferable to {@link #add}, which can fail to insert an element only
     * by throwing an exception.
     *
     * @param e the element to add
     * @return <tt>true</tt> if the element was added to this queue, else
     * <tt>false</tt>
     * @throws ClassCastException       if the class of the specified element
     *                                  prevents it from being added to this queue
     * @throws NullPointerException     if the specified element is null and
     *                                  this queue does not permit null elements
     * @throws IllegalArgumentException if some property of this element
     *                                  prevents it from being added to this queue
     */
    @Override
    public boolean offer(E e) {
        throw new UnsupportedOperationException("Used offerAndRecycle instead");
    }

    public boolean offerAndRecycle(Object...params) {
        addAndRecycle(params);
        return true;
    }

    /**
     * Retrieves and removes the head of this queue.  This method differs
     * from {@link #poll poll} only in that it throws an exception if this
     * queue is empty.
     *
     * @return the head of this queue
     * @throws NoSuchElementException if this queue is empty
     */
    @Override
    public E remove() {
        if (used > 0)
            return (E)items[--used];
        else
            throw new NoSuchElementException();
    }

    /**
     * Retrieves and removes the head of this queue,
     * or returns <tt>null</tt> if this queue is empty.
     *
     * @return the head of this queue, or <tt>null</tt> if this queue is empty
     */
    @Override
    public E poll() {
        if (used > 0)
            return (E)items[--used];
        else
            return null;
    }

    /**
     * Retrieves, but does not remove, the head of this queue.  This method
     * differs from {@link #peek peek} only in that it throws an exception
     * if this queue is empty.
     *
     * @return the head of this queue
     * @throws NoSuchElementException if this queue is empty
     */
    @Override
    public E element() {
        if (used > 0)
            return (E)items[used - 1];
        else
            throw new NoSuchElementException();
    }

    /**
     * Retrieves, but does not remove, the head of this queue,
     * or returns <tt>null</tt> if this queue is empty.
     *
     * @return the head of this queue, or <tt>null</tt> if this queue is empty
     */
    @Override
    public E peek() {
        if (used > 0)
            return (E)items[used - 1];
        else
            return null;
    }
}
