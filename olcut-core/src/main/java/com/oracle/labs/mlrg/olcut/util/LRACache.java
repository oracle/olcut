
package com.oracle.labs.mlrg.olcut.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A least-recently-accessed cache.
 */
public class LRACache<K,V> extends LinkedHashMap<K,V> {

    /**
     * The number of items to be held in the cache.  If the size is less
     * than 0, no elements will ever be removed.
     */
    protected int lraSize;

    /**
     * The default cache size.
     */
    protected static final int DEFAULT_SIZE = 200;

    /**
     * Creates a cache of the default size.
     */
    public LRACache() {
        this(DEFAULT_SIZE);
    } // LRACache constructor
    
    /**
     * Creates a cache that will hold the given number of items.
     * @param size The number of elements to cache.  If the size is less
     * than 0, then the cache can grow unboundedly.
     */
    public LRACache(int size) {
        super(size > 0 ? size : DEFAULT_SIZE, 0.75f, true);
        lraSize = size;
    } // LRACache constructor

    /**
     * Removes the oldest entry in the map.
     * @param eldest The oldest entry.
     * @return <CODE>true</CODE> if this entry should be removed, which
     * will happen when the size of the map exceeds our cache size.
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return lraSize > 0 && size() > lraSize;
    }
    
} // LRACache
