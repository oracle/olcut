/*
 * Copyright 2007-2008 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.labs.util;

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
