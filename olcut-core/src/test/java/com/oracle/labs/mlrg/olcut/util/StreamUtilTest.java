package com.oracle.labs.mlrg.olcut.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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

import org.junit.Test;

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
        assertNotEquals("Parallelism wasn't bounded", boundedCounter.get(), unboundedCounter.get());
        assertEquals("Parallelism wasn't bounded", 2 << 2, boundedCounter.get());
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
