/*
 * Copyright 2015 Oracle Corporation.
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
