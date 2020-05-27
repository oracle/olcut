
/*
 * Copyright (c) 2004-2020, Oracle and/or its affiliates.
 *
 * Licensed under the 2-clause BSD license.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

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
