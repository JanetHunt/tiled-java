/*
 *  Tiled Map Editor, (c) 2004-2006
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Adam Turk <aturk@biggeruniverse.com>
 *  Bjorn Lindeijer <bjorn@lindeijer.nl>
 */

package tiled.util;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A NumberedSet is a generic container of Objects &lt;E&gt; where each element is
 * identified by an integer id (key).  The set of ids for a NumberedSet may 
 * not be contiguous. The mapping between id and element remains unaffected 
 * when elements are deleted.  While basically being a map, this structure 
 * also works as a list through its simple <code>add(E)</code>
 * and <code>iterator()</code> methods. 
 * <p>Implementation: When putting map elements, high integer values should
 * be avoided since the entire array space to the highest key value is claimed.
 *
 * @author rainerd
 * @author janetHunt modified and corrected, 10.2012
 */
public class NumberedSet<E> implements Cloneable, Iterable<E>
{
    private ArrayList<E> data;
    private int size;
    private int modifyNr;

    /**
     * Constructs a new empty NumberedSet.
     */
    public NumberedSet() {
        data = new ArrayList<E>();
    }

    
    /**
     * Returns a shallow clone of this NumberedSet.
     * @return Object == <code>NumberedSet</code> 
     */
    @SuppressWarnings("unchecked")
    @Override
    public synchronized Object clone() {
        try {
            NumberedSet<E> c = (NumberedSet<E>)super.clone(); 
            c.data = (ArrayList<E>)data.clone();
            return c;
        } catch ( CloneNotSupportedException e ) 
        { return null; }
    }

    /**
     * Returns the element for a specific element, or <b>null</b> if the id does not
     * identify any element in this <code>NumberedSet</code>.
     *
     * @param id
     * @return E element value currently associated with this id or <b>null</b>
     */
    public synchronized E get( int id ) {
        try {
            return data.get(id);
        } catch (IndexOutOfBoundsException e) {}

        return null;
    }

    /**
     * Returns true if this <code>NumberedSet</code> contains an element for the specified id.
     *
     * @param id int element name (id number)
     * @return boolean <b>true</b> if an element with this name exists
     */
    public synchronized boolean containsId( int id ) {
        return get(id) != null;
    }

    /**
     * Sets the element value for the specified id, replacing any previous value that
     * may have been associated with that id. (id should be a relatively small positive
     * integer.)
     *
     * @param id int element name (id number)
     * @param o E element value to be set (null allowed)
     * @return E the element value previously associated with this id or <b>null</b>
     * @throws IllegalArgumentException for negative id
     */
    public synchronized E put( int id, E o ) throws IllegalArgumentException {
        if (id < 0) {
            throw new IllegalArgumentException();
        }
        
        E result;
        if (o == null) {
        	// act as "remove" if object is null
        	result = remove(id);
        } else {
	        // Make sure there is sufficient space to overlay
	        for (int i = id - data.size(); i >= 0; i--) {
	            data.add(null);
	        }
	        if ( (result=data.set(id, o)) == null) {
	        	size++;
	        	modifyNr++;
	        }
        }
        return result;
    }

    /** Removes all elements from in this NumberedSet.
     * After performing this method the size of the set is zero.
     */
    public synchronized void clear() {
        data.clear();
        size = 0;
        modifyNr++;
    }

    /**
     * Removes the element associated with the given id from the NumberedSet.
     * (key remove)
     *
     * @param id int object number (name)
     * @return the object previously stored at this id or <b>null</b> if nothing was stored
     */
    public synchronized E remove(int id) {
        E obj = null;
    	if (id >= 0 && id < data.size() &&
    	    (obj=data.set(id, null)) != null ) {
    	    	size--;
	        	modifyNr++;
   	    }
    	return obj;
    }

    /** 
     * Removes the given element from this NumberedSet (value remove).
     * 
     * @param obj E element value to be removed
     * @return boolean <b>true</b> if and only if the value has been present and was removed
     */
    public synchronized boolean remove ( E obj ) {
        int index = getIdOf(obj);
        return remove(index) != null;
    }
    
    /**
     * Returns the last id in the NumberedSet that is associated with an
     * element, or -1 if the set is empty.
     *
     * @return int last in list object number or -1 if empty
     */
    public synchronized int getLastId() {
        int maxId = data.size() - 1;
        while (maxId >= 0) {
            if (containsId(maxId)) {
                break;
            }
            maxId--;
        }
        return maxId;
    }

    /**
     * Returns an iterator over all elements of this <code>NumberedSet</code>.
     * (Does not return <b>null</b> object references.)
     *
     * @return NumberedSetIterator
     */
    public synchronized Iterator<E> iterator() {
        return new NumberedSetIterator();
    }

    /**
     * Adds a new element to the <code>NumberedSet</code> and returns its id.
     * (This method may return an id number previously used on a removed element.
     * Principally this method will never fail to add a new element except
     * when hitting an out-of-memory VM condition.)
     *
     * @param obj E element value to add
     * @return int element id number or -1 if obj was <b>null</b>
     */
    public synchronized int add( E obj ) {
    	int id = -1;
    	if (obj != null) {
    		id = getLastId() + 1;
    		put(id, obj);
    	}
        return id;
    }

    /**
     * Returns the id of the first element of this <code>NumberedSet</code> that is 
     * equal to the given object, or -1 otherwise.
     *
     * @param obj E element value to be searched
     * @return element name (id) or -1 if not found
     */
    public synchronized int getIdOf( E obj ) {
        return data.indexOf(obj);
    }

    /**
     * Returns true if at least one element of this <code>NumberedSet</code> is equal 
     * to the given object.
     *
     * @param obj E element value to be searched
     * @return boolean <b>true</b> if and only if <code>obj</code> is an element value in this set
     */
    public synchronized boolean contains( E obj ) {
        return data.contains(obj);
    }

    /**
     * If this NumberedSet already contains an element equal to the given object,
     * return its id.  Otherwise insert the given object into the NumberedSet
     * and return resulting id.
     */
    public synchronized int ensureElement( E obj ) {
        int id = getIdOf(obj);
        return id != -1 ? id : add(obj);
    }

    /**
     * Returns the number of actual elements in this <code>NumberedSet</code>.
     *
     * @return int number of (non-null) elements
     */
    public synchronized int size() {
        return size;
    }

    /** Whether this NumberedSet has size zero.
     * @return boolean
     */
    public boolean isEmpty () {
        return size() == 0;
    }
    
    /** Iterator over NumberSet valid objects. This iterator skips all null
     * occurrences of objects in underlying data list.
     * @author janetHunt
     */
    private class NumberedSetIterator implements Iterator<E> {
    	private Iterator<E> rawIter = data.iterator();
    	private E next;
    	private int modStamp = modifyNr;
    	
		@Override
		public boolean hasNext() {
			if (modStamp != modifyNr) {
				throw new ConcurrentModificationException();
			}
			if (next == null) {
				try { 
					// attempt superior iterator, skipping null occurrences
					do { next = rawIter.next(); }
					while (next == null);
				} catch (NoSuchElementException e) {}
			}
			return next != null;
		}

		@Override
		public E next() {
			E n = next;
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			next = null;
			return n;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();			
		}
    }
}
