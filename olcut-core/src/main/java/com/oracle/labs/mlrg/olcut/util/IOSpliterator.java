/*
 * Copyright (c) 2015-2020, Oracle and/or its affiliates.
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

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * A {@link Spliterator} which doesn't grow it's buffer.
 * @param <T> The type of the contained object.
 */
public abstract class IOSpliterator<T> implements Spliterator<T> {
    public static final int DEFAULT_BATCH_SIZE = 256;
    public static final int DEFAULT_CHARACTERISTICS = Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED;

    private final int characteristics;
    private final int batchSize;
    private long estimatedSize;
    
    public IOSpliterator(int characteristics, int batchSize, long estimatedSize) {
        this.characteristics = characteristics | Spliterator.SUBSIZED;
        this.batchSize = batchSize;
        this.estimatedSize = estimatedSize;
    }

    public IOSpliterator(int characteristics, long estimatedSize) {
        this(characteristics,DEFAULT_BATCH_SIZE,estimatedSize);
    }

    public IOSpliterator(int characteristics) {
        this(characteristics,DEFAULT_BATCH_SIZE,Long.MAX_VALUE);
    }

    public IOSpliterator() {
        this(DEFAULT_CHARACTERISTICS);
    }

    @Override
    public Spliterator<T> trySplit() {
        final BufferConsumer<T> holder = new BufferConsumer<>();
        if (!tryAdvance(holder)) {
            return null;
        }
        
        final Object[] a = new Object[batchSize];
        int j = 0;
        do {
            a[j] = holder.value;
        } while (++j < batchSize && tryAdvance(holder));
        
        if (estimatedSize != Long.MAX_VALUE) {
            estimatedSize -= j;
        }
        return Spliterators.spliterator(a, 0, j, characteristics() | SIZED);
    }

    @Override
    public long estimateSize() {
        return estimatedSize;
    }

    @Override
    public int characteristics() {
        return characteristics;
    }
    
    static final class BufferConsumer<T> implements Consumer<T> {
        T value;

        @Override
        public void accept(T value) {
            this.value = value;
        }
    }
}
