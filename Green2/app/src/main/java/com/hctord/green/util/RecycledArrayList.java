package com.hctord.green.util;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by HéctorD on 5/3/2014.
 */
public class RecycledArrayList<E> implements List<E> {

    public final Comparer<E> DEFAULT_COMPARER = new Comparer<E>() {
        @Override
        public boolean compare(E a, E b) {
            return a.equals(b);
        }
    };

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

    private class ListIterator implements java.util.ListIterator<E> {
        private int index = 0;

        /**
         * Inserts the specified object into the list between {@code next} and
         * {@code previous}. The object inserted will be the previous object.
         *
         * @param object the object to insert.
         * @throws UnsupportedOperationException if adding is not supported by the list being iterated.
         * @throws ClassCastException            if the class of the object is inappropriate for the list.
         * @throws IllegalArgumentException      if the object cannot be added to the list.
         */
        @Override
        public void add(E object) {
            throw new UnsupportedOperationException();
        }

        /**
         * Returns whether there are more elements to iterate.
         *
         * @return {@code true} if there are more elements, {@code false} otherwise.
         * @see #next
         */
        @Override
        public boolean hasNext() {
            return index < size();
        }

        /**
         * Returns whether there are previous elements to iterate.
         *
         * @return {@code true} if there are previous elements, {@code false}
         * otherwise.
         * @see #previous
         */
        @Override
        public boolean hasPrevious() {
            return index > 0;
        }

        /**
         * Returns the next object in the iteration.
         *
         * @return the next object.
         * @see #hasNext
         * @throws NoSuchElementException if there are no more elements.
         */
        @Override
        public E next() {
            if (hasNext())
                return (E)items[index++];
            else
                throw new NoSuchElementException();
        }

        /**
         * Returns the index of the next object in the iteration.
         *
         * @return the index of the next object, or the size of the list if the
         * iterator is at the end.
         * @throws NoSuchElementException if there are no more elements.
         * @see #next
         */
        @Override
        public int nextIndex() {
            if (hasNext())
                return index + 1;
            else
                throw new NoSuchElementException();
        }

        /**
         * Returns the previous object in the iteration.
         *
         * @return the previous object.
         * @throws NoSuchElementException if there are no previous elements.
         * @see #hasPrevious
         */
        @Override
        public E previous() {
            if (hasPrevious())
                return (E)items[index--];
            else
                throw new NoSuchElementException();
        }

        /**
         * Returns the index of the previous object in the iteration.
         *
         * @return the index of the previous object, or -1 if the iterator is at the
         * beginning.
         * @throws NoSuchElementException if there are no previous elements.
         * @see #previous
         */
        @Override
        public int previousIndex() {
            if (hasPrevious())
                return index - 1;
            else
                throw new NoSuchElementException();
        }

        /**
         * Removes the last object returned by {@code next} or {@code previous} from
         * the list.
         *
         * @throws UnsupportedOperationException if removing is not supported by the list being iterated.
         * @throws IllegalStateException         if {@code next} or {@code previous} have not been called, or
         *                                       {@code remove} or {@code add} have already been called after
         *                                       the last call to {@code next} or {@code previous}.
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /**
         * Replaces the last object returned by {@code next} or {@code previous}
         * with the specified object.
         *
         * @param object the object to set.
         * @throws UnsupportedOperationException if setting is not supported by the list being iterated
         * @throws ClassCastException            if the class of the object is inappropriate for the list.
         * @throws IllegalArgumentException      if the object cannot be added to the list.
         * @throws IllegalStateException         if {@code next} or {@code previous} have not been called, or
         *                                       {@code remove} or {@code add} have already been called after
         *                                       the last call to {@code next} or {@code previous}.
         */
        @Override
        public void set(E object) {
            throw new UnsupportedOperationException();
        }
    }

    private Object[] items;
    private int used;
    private int interval;
    private Recycler<E> recycler;
    private Comparer<E> comparer;
    private Class<E> eClass;

    public RecycledArrayList(Recycler<E> recycler, Class<E> eClass) {
        this(recycler, null, eClass);
        comparer = DEFAULT_COMPARER;
    }

    public RecycledArrayList(Recycler<E> recycler, Comparer<E> comparer, Class<E> eClass) {
        this(recycler, comparer, eClass, 4);
    }

    public RecycledArrayList(Recycler<E> recycler, Comparer<E> comparer, Class<E> eClass,
                             int initialCapacity) {
        this(recycler, comparer, eClass, initialCapacity, 0);
    }

    public RecycledArrayList(Recycler<E> recycler, Comparer<E> comparer, Class<E> eClass,
                             int initialCapacity, int interval) {
        this.recycler = recycler;
        used = 0;
        items = new Object[initialCapacity];
        this.interval = interval;
        this.eClass = eClass;
        this.comparer = comparer;
        // Test creating new instance.
        try {
            eClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Type has no public default constructor or " +
                    "error occurred during instantiation");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Type has no public default constructor.");
        }
    }

    public void addAndRecycle(Object...params) {
        if (used == items.length) {
            Object[] newItems = new Object[
                    interval == 0 ? used * 2 : used + interval
                    ];

            System.arraycopy(items, 0, newItems, 0, used);
            items = newItems;
        }
        if (items[used] == null) {
            try {
                items[used] = eClass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        recycler.recycle((E)items[used++], params);
    }

    public void addAndRecycleAtPosition(int location, Object...params) {
        if (location < 0 || location > size())
            throw new IndexOutOfBoundsException();

        if (used == items.length) {
            Object[] newItems = new Object[
                    interval == 0 ? used * 2 : used + interval
                    ];

            System.arraycopy(items, 0, newItems, 0, used);
            items = newItems;
        }

        System.arraycopy(items, location, items, location + 1, used - location);

        if (items[location] == null) {
            try {
                items[location] = eClass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        ++used;
        recycler.recycle((E)items[location], params);
    }

    /**
     * Inserts the specified object into this {@code List} at the specified location.
     * The object is inserted before the current element at the specified
     * location. If the location is equal to the size of this {@code List}, the object
     * is added at the end. If the location is smaller than the size of this
     * {@code List}, then all elements beyond the specified location are moved by one
     * position towards the end of the {@code List}.
     *
     * @param location the index at which to insert.
     * @param object   the object to add.
     * @throws UnsupportedOperationException if adding to this {@code List} is not supported.
     * @throws ClassCastException            if the class of the object is inappropriate for this
     *                                       {@code List}.
     * @throws IllegalArgumentException      if the object cannot be added to this {@code List}.
     * @throws IndexOutOfBoundsException     if {@code location < 0 || location > size()}
     */
    @Override
    public void add(int location, E object) {
        throw new UnsupportedOperationException("Use addAndRecycle instead");
    }

    /**
     * Adds the specified object at the end of this {@code List}.
     *
     * @param object the object to add.
     * @return always true.
     * @throws UnsupportedOperationException if adding to this {@code List} is not supported.
     * @throws ClassCastException            if the class of the object is inappropriate for this
     *                                       {@code List}.
     * @throws IllegalArgumentException      if the object cannot be added to this {@code List}.
     */
    @Override
    public boolean add(E object) {
        throw new UnsupportedOperationException("Use addAndRecycleInstead");
    }

    /**
     * Inserts the objects in the specified collection at the specified location
     * in this {@code List}. The objects are added in the order they are returned from
     * the collection's iterator.
     *
     * @param location   the index at which to insert.
     * @param collection the collection of objects to be inserted.
     * @return true if this {@code List} has been modified through the insertion, false
     * otherwise (i.e. if the passed collection was empty).
     * @throws UnsupportedOperationException if adding to this {@code List} is not supported.
     * @throws ClassCastException            if the class of an object is inappropriate for this
     *                                       {@code List}.
     * @throws IllegalArgumentException      if an object cannot be added to this {@code List}.
     * @throws IndexOutOfBoundsException     if {@code location < 0 || location > size()}
     */
    @Override
    public boolean addAll(int location, Collection<? extends E> collection) {
        return false;
    }

    /**
     * Adds the objects in the specified collection to the end of this {@code List}. The
     * objects are added in the order in which they are returned from the
     * collection's iterator.
     *
     * @param collection the collection of objects.
     * @return {@code true} if this {@code List} is modified, {@code false} otherwise
     * (i.e. if the passed collection was empty).
     * @throws UnsupportedOperationException if adding to this {@code List} is not supported.
     * @throws ClassCastException            if the class of an object is inappropriate for this
     *                                       {@code List}.
     * @throws IllegalArgumentException      if an object cannot be added to this {@code List}.
     */
    @Override
    public boolean addAll(Collection<? extends E> collection) {
        return false;
    }

    /**
     * Removes all elements from this {@code List}, leaving it empty.
     *
     * @throws UnsupportedOperationException if removing from this {@code List} is not supported.
     * @see #isEmpty
     * @see #size
     */
    @Override
    public void clear() {
        used = 0;
    }

    /**
     * Tests whether this {@code List} contains the specified object.
     *
     * @param object the object to search for.
     * @return {@code true} if object is an element of this {@code List}, {@code false}
     * otherwise
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
     * Tests whether this {@code List} contains all objects contained in the
     * specified collection.
     *
     * @param collection the collection of objects
     * @return {@code true} if all objects in the specified collection are
     * elements of this {@code List}, {@code false} otherwise.
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
     * Returns the element at the specified location in this {@code List}.
     *
     * @param location the index of the element to return.
     * @return the element at the specified location.
     * @throws IndexOutOfBoundsException if {@code location < 0 || location >= size()}
     */
    @Override
    public E get(int location) {
        if (location < 0 || location >= size())
            return null;

        return (E)items[location];
    }

    /**
     * Searches this {@code List} for the specified object and returns the index of the
     * first occurrence.
     *
     * @param object the object to search for.
     * @return the index of the first occurrence of the object or -1 if the
     * object was not found.
     */
    @Override
    public int indexOf(Object object) {
        if (object == null)
            return -1;

        for (int i = 0; i < used; i++) {
            if (comparer.compare((E)items[i], (E)object))
                return i;
        }
        return -1;
    }

    /**
     * Returns whether this {@code List} contains no elements.
     *
     * @return {@code true} if this {@code List} has no elements, {@code false}
     * otherwise.
     * @see #size
     */
    @Override
    public boolean isEmpty() {
        return used == 0;
    }

    /**
     * Returns an iterator on the elements of this {@code List}. The elements are
     * iterated in the same order as they occur in the {@code List}.
     *
     * @return an iterator on the elements of this {@code List}.
     * @see java.util.Iterator
     */
    @Override
    public java.util.Iterator iterator() {
        return new Iterator();
    }

    /**
     * Searches this {@code List} for the specified object and returns the index of the
     * last occurrence.
     *
     * @param object the object to search for.
     * @return the index of the last occurrence of the object, or -1 if the
     * object was not found.
     */
    @Override
    public int lastIndexOf(Object object) {
        int index = -1;
        for (int i = 0; i < used; i++) {
            if (comparer.compare((E)items[i], (E)object))
                index = i;
        }
        return index;
    }

    /**
     * Returns a {@code List} iterator on the elements of this {@code List}. The elements are
     * iterated in the same order that they occur in the {@code List}.
     *
     * @return a {@code List} iterator on the elements of this {@code List}
     * @see java.util.ListIterator
     */
    @Override
    public java.util.ListIterator listIterator() {
        return new ListIterator();
    }

    /**
     * Returns a list iterator on the elements of this {@code List}. The elements are
     * iterated in the same order as they occur in the {@code List}. The iteration
     * starts at the specified location.
     *
     * @param location the index at which to start the iteration.
     * @return a list iterator on the elements of this {@code List}.
     * @throws IndexOutOfBoundsException if {@code location < 0 || location > size()}
     * @see java.util.ListIterator
     */
    @Override
    public java.util.ListIterator listIterator(int location) {
        ListIterator rli = new ListIterator();
        rli.index = location;
        return rli;
    }

    /**
     * Removes the object at the specified location from this {@code List}.
     *
     * @param location the index of the object to remove.
     * @return the removed object.
     * @throws UnsupportedOperationException if removing from this {@code List} is not supported.
     * @throws IndexOutOfBoundsException     if {@code location < 0 || location >= size()}
     */
    @Override
    public E remove(int location) {
        if (location < 0 || location >= size())
            throw new IndexOutOfBoundsException();

        E removed = (E)items[location];
        --used;
        for (int i = location; i < used; i++) {
            items[i] = items[i + 1];
        }
        return removed;
    }

    /**
     * Removes the first occurrence of the specified object from this {@code List}.
     *
     * @param object the object to remove.
     * @return true if this {@code List} was modified by this operation, false
     * otherwise.
     * @throws UnsupportedOperationException if removing from this {@code List} is not supported.
     */
    @Override
    public boolean remove(Object object) {
        int index = indexOf(object);
        if (index < 0)
            return false;
        remove(index);
        return true;
    }

    /**
     * Removes all occurrences in this {@code List} of each object in the specified
     * collection.
     *
     * @param collection the collection of objects to remove.
     * @return {@code true} if this {@code List} is modified, {@code false} otherwise.
     * @throws UnsupportedOperationException if removing from this {@code List} is not supported.
     */
    @Override
    public boolean removeAll(Collection<?> collection) {
        return false;
    }

    /**
     * Removes all objects from this {@code List} that are not contained in the
     * specified collection.
     *
     * @param collection the collection of objects to retain.
     * @return {@code true} if this {@code List} is modified, {@code false} otherwise.
     * @throws UnsupportedOperationException if removing from this {@code List} is not supported.
     */
    @Override
    public boolean retainAll(Collection<?> collection) {
        return false;
    }

    /**
     * Replaces the element at the specified location in this {@code List} with the
     * specified object. This operation does not change the size of the {@code List}.
     *
     * @param location the index at which to put the specified object.
     * @param object   the object to insert.
     * @return the previous element at the index.
     * @throws UnsupportedOperationException if replacing elements in this {@code List} is not supported.
     * @throws ClassCastException            if the class of an object is inappropriate for this
     *                                       {@code List}.
     * @throws IllegalArgumentException      if an object cannot be added to this {@code List}.
     * @throws IndexOutOfBoundsException     if {@code location < 0 || location >= size()}
     */
    @Override
    public E set(int location, E object) {
        E obj = (E)items[location];
        items[location] = object;
        return obj;
    }

    /**
     * Returns the number of elements in this {@code List}.
     *
     * @return the number of elements in this {@code List}.
     */
    @Override
    public int size() {
        return used;
    }

    /**
     * Returns a {@code List} of the specified portion of this {@code List} from the given start
     * index to the end index minus one. The returned {@code List} is backed by this
     * {@code List} so changes to it are reflected by the other.
     *
     * @param start the index at which to start the sublist.
     * @param end   the index one past the end of the sublist.
     * @return a list of a portion of this {@code List}.
     * @throws IndexOutOfBoundsException if {@code start < 0, start > end} or {@code end >
     *                                   size()}
     */
    @Override
    public List<E> subList(int start, int end) {
        return null;
    }

    /**
     * Returns an array containing all elements contained in this {@code List}.
     *
     * @return an array of the elements from this {@code List}.
     */
    @Override
    public Object[] toArray() {
        return items.clone();
    }

    /**
     * Returns an array containing all elements contained in this {@code List}. If the
     * specified array is large enough to hold the elements, the specified array
     * is used, otherwise an array of the same type is created. If the specified
     * array is used and is larger than this {@code List}, the array element following
     * the collection elements is set to null.
     *
     * @param array the array.
     * @return an array of the elements from this {@code List}.
     * @throws ArrayStoreException if the type of an element in this {@code List} cannot be stored
     *                             in the type of the specified array.
     */
    @Override
    public <T1> T1[] toArray(T1[] array) {
        return null;
    }

}
