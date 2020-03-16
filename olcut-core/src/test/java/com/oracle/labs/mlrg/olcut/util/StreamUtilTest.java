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


import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


public class StreamUtilTest {
    private static final Logger logger = Logger.getLogger(StreamUtilTest.class.getName());

    @Test
    public void testBoundedParallelism() throws ExecutionException, InterruptedException {
        ForkJoinPool fjp = new ForkJoinPool(2);

        AtomicInteger unboundedCounter = new AtomicInteger();
        Stream<Integer> unbounded = wrapStream(StreamUtil.boundParallelism(arrayFactory(10000).parallel()),unboundedCounter);
        List<Integer> output = unbounded.map(a -> a + 5).collect(Collectors.toList());

        AtomicInteger boundedCounter = new AtomicInteger();
        Stream<Integer> bounded = wrapStream(StreamUtil.boundParallelism(arrayFactory(10000).parallel()),boundedCounter);
        List<Integer> otherOutput = fjp.submit(() -> bounded.map(a -> a + 5).collect(Collectors.toList())).get();

        logger.finer("Unbounded = " + unboundedCounter.get() + ", bounded = " + boundedCounter.get());
        assertNotEquals(boundedCounter.get(), unboundedCounter.get(), "Parallelism wasn't bounded");
        assertEquals(2 << 2, boundedCounter.get(), "Parallelism wasn't bounded");
    }

    public static Stream<Integer> arrayFactory(int size) {
        Integer[] array = new Integer[size];
        for (int i = 0; i < size; i++) {
            array[i] = i;
        }

        return Arrays.stream(array);
    }

    public static <T> Stream<T> wrapStream(Stream<T> stream, AtomicInteger counter) {
        Spliterator<T> countingSpliterator = new CountingSpliterator<>(stream.spliterator(),counter);

        return StreamSupport.stream(countingSpliterator,true);
    }

    private static class CountingSpliterator<T> implements Spliterator<T> {
        private final AtomicInteger taskCounter;
        private final Spliterator<T> spliterator;

        public CountingSpliterator(Spliterator<T> spliterator, AtomicInteger taskCounter) {
            this.spliterator = spliterator;
            this.taskCounter = taskCounter;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            return spliterator.tryAdvance(action);
        }

        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            if (!(spliterator instanceof CountingSpliterator)) {
                taskCounter.incrementAndGet();
            }
            spliterator.forEachRemaining(action);
        }

        @Override
        public Spliterator<T> trySplit() {
            Spliterator<T> tmp = spliterator.trySplit();
            if (tmp != null) {
                return new CountingSpliterator<>(tmp,taskCounter);
            } else {
                return null;
            }
        }

        @Override
        public long estimateSize() {
            return spliterator.estimateSize();
        }

        @Override
        public long getExactSizeIfKnown() {
            return spliterator.getExactSizeIfKnown();
        }

        @Override
        public Comparator<? super T> getComparator() {
            return spliterator.getComparator();
        }

        @Override
        public int characteristics() {
            return spliterator.characteristics();
        }
    }
}
