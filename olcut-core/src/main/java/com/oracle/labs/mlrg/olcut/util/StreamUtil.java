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

import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utilities for operating on streams.
 */
public final class StreamUtil {

    private StreamUtil(){}

    /**
     * An object instance representing no value, that cannot be an actual
     * data element of a stream.  Used when processing streams that can contain
     * {@code null} elements to distinguish between a {@code null} value and no
     * value.
     */
    static final Object NONE = new Object();

    /**
     * Takes a stream and returns a stream which bounds the parallelism available
     * based on the size of the {@link ForkJoinPool} it is executing in.
     * <p>
     * This is a workaround for code running on Java 8 and 9, as it's fixed in Java 10 which
     * makes all {@link Stream}s have this property.
     * <p>
     * @param inputStream A Stream of T.
     * @param <T> The type of the Stream.
     * @return A Stream which bounds the parallelism to the size of the FJP.
     */
    public static <T> Stream<T> boundParallelism(Stream<T> inputStream) {
        Spliterator<T> boundedSpliterator = new BoundedSpliterator<>(inputStream.spliterator());

        return StreamSupport.stream(boundedSpliterator,true);
    }
    
    /**
     * Creates a lazy and sequential combined {@link Stream} whose elements are
     * the result of combining the elements of two streams.  The resulting
     * stream is ordered if both of the input streams are ordered.  The size of
     * the resulting stream will be the smaller of the sizes of the two input
     * streams; any elements remaining in the larger of the two streams will not
     * be consumed.
     *
     * @param <A> the type of elements of the first {@code Stream}
     * @param <B> the type of elements of the second {@code Stream}
     * @param <C> the type of elements of the zipped {@code Stream}
     * @param a the first {@code Stream} to combine
     * @param b the second {@code Stream} to combine
     * @param zipper a function applied to an element from the first
     *        {@code Stream} and an element from the second {@code Stream} to
     *        produce an element for the combined {@code Stream}
     * @return a combined {@code Stream}
     */
    public static <A, B, C> Stream<C> zip(Stream<? extends A> a,
                                          Stream<? extends B> b,
                                          BiFunction<? super A, ? super B, ? extends C> zipper) {
        Objects.requireNonNull(zipper);
        @SuppressWarnings("unchecked")
        Spliterator<A> as = (Spliterator<A>) Objects.requireNonNull(a).spliterator();
        @SuppressWarnings("unchecked")
        Spliterator<B> bs = (Spliterator<B>) Objects.requireNonNull(b).spliterator();

        // Combining loses DISTINCT and SORTED characteristics and for other
        // characteristics the combined stream has a characteristic if both
        // streams to combine have the characteristic
        int characteristics = as.characteristics() & bs.characteristics() &
                              ~(Spliterator.DISTINCT | Spliterator.SORTED);
        long size = Math.min(as.estimateSize(), bs.estimateSize());

        Spliterator<C> cs = new ZipperSpliterator<>(as, bs, zipper,
                                                    size, characteristics);
        return (a.isParallel() || b.isParallel())
               ? StreamSupport.stream(cs,true)
               : StreamSupport.stream(cs,false);
    }
    
    private static final class ZipperSpliterator<A, B, C> extends Spliterators.AbstractSpliterator<C>
            implements Consumer<Object> {
        final Spliterator<A> as;
        final Spliterator<B> bs;
        final BiFunction<? super A, ? super B, ? extends C> zipper;
        Object a;
        Object b;

        ZipperSpliterator(Spliterator<A> as, Spliterator<B> bs,
                          BiFunction<? super A, ? super B, ? extends C> zipper,
                          long est, int additionalCharacteristics) {
            super(est, additionalCharacteristics);
            this.as = as;
            this.bs = bs;
            this.zipper = zipper;
            this.a = StreamUtil.NONE;
        }

        @Override
        public void accept(Object aOrB) {
            if (a == StreamUtil.NONE) {
                a = aOrB;
            }
            else {
                b = aOrB;
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean tryAdvance(Consumer<? super C> action) {
            if (as.tryAdvance(this) && bs.tryAdvance(this)) {
                Object aTmp = this.a;
                this.a = StreamUtil.NONE;
                action.accept(zipper.apply((A) aTmp, (B) b));
                return true;
            }
            return false;
        }
    }

    /**
     * The same as {@link StreamUtil#zip} but generates an {@link IOSpliterator} in the
     * stream.
     * <p>
     * Creates a lazy and sequential combined {@link Stream} whose elements are
     * the result of combining the elements of two streams.  The resulting
     * stream is ordered if both of the input streams are ordered.  The size of
     * the resulting stream will be the smaller of the sizes of the two input
     * streams; any elements remaining in the larger of the two streams will not
     * be consumed.
     * <p>
     * @param <A> the type of elements of the first {@code Stream}
     * @param <B> the type of elements of the second {@code Stream}
     * @param <C> the type of elements of the zipped {@code Stream}
     * @param a the first {@code Stream} to combine
     * @param b the second {@code Stream} to combine
     * @param zipper a function applied to an element from the first
     *        {@code Stream} and an element from the second {@code Stream} to
     *        produce an element for the combined {@code Stream}
     * @return a combined {@code Stream}
     */
    public static <A, B, C> Stream<C> zipIO(Stream<? extends A> a,
                                            Stream<? extends B> b,
                                            BiFunction<? super A, ? super B, ? extends C> zipper) {
        Objects.requireNonNull(zipper);
        @SuppressWarnings("unchecked")
        Spliterator<A> as = (Spliterator<A>) Objects.requireNonNull(a).spliterator();
        @SuppressWarnings("unchecked")
        Spliterator<B> bs = (Spliterator<B>) Objects.requireNonNull(b).spliterator();

        // Combining loses DISTINCT and SORTED characteristics and for other
        // characteristics the combined stream has a characteristic if both
        // streams to combine have the characteristic
        int characteristics = as.characteristics() & bs.characteristics() &
                              ~(Spliterator.DISTINCT | Spliterator.SORTED);
        long size = Math.min(as.estimateSize(), bs.estimateSize());

        Spliterator<C> cs = new ZipperSpliteratorIO<>(as, bs, zipper,
                                                    size, characteristics);
        return (a.isParallel() || b.isParallel())
               ? StreamSupport.stream(cs,true)
               : StreamSupport.stream(cs,false);
    }

    private static final class ZipperSpliteratorIO<A, B, C> extends IOSpliterator<C>
            implements Consumer<Object> {
        final Spliterator<A> as;
        final Spliterator<B> bs;
        final BiFunction<? super A, ? super B, ? extends C> zipper;
        Object a;
        Object b;

        ZipperSpliteratorIO(Spliterator<A> as, Spliterator<B> bs,
                          BiFunction<? super A, ? super B, ? extends C> zipper,
                          long est, int additionalCharacteristics) {
            super(additionalCharacteristics, est);
            this.as = as;
            this.bs = bs;
            this.zipper = zipper;
            this.a = StreamUtil.NONE;
        }

        @Override
        public void accept(Object aOrB) {
            if (a == StreamUtil.NONE) {
                a = aOrB;
            }
            else {
                b = aOrB;
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean tryAdvance(Consumer<? super C> action) {
            if (as.tryAdvance(this) && bs.tryAdvance(this)) {
                Object aTmp = this.a;
                this.a = StreamUtil.NONE;
                action.accept(zipper.apply((A) aTmp, (B) b));
                return true;
            }
            return false;
        }
    }

    /**
     * A spliterator with bounded parallelism, it calculates the target size
     * based upon the number of threads available in the ForkJoinPool it executes in.
     * @param <T> The element type of the spliterator.
     */
    private static class BoundedSpliterator<T> implements Spliterator<T> {
        private static final Logger logger = Logger.getLogger(BoundedSpliterator.class.getName());

        private final Spliterator<T> spliterator;
        private long targetSize = -1L;

        public BoundedSpliterator(Spliterator<T> spliterator) {
            this.spliterator = spliterator;
        }

        public BoundedSpliterator(Spliterator<T> spliterator, long targetSize) {
            this.spliterator = spliterator;
            this.targetSize = targetSize;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            return spliterator.tryAdvance(action);
        }

        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            spliterator.forEachRemaining(action);
        }

        @Override
        public Spliterator<T> trySplit() {
            if (targetSize == -1L) {
                Thread curThread = Thread.currentThread();
                if (curThread instanceof ForkJoinWorkerThread) {
                    targetSize = spliterator.estimateSize() / (((ForkJoinWorkerThread) curThread).getPool().getParallelism() << 2);
                    logger.log(Level.FINEST,"In FJP - setting targetSize to " + targetSize);
                } else {
                    targetSize = spliterator.estimateSize() / (ForkJoinPool.getCommonPoolParallelism() << 2);
                    logger.log(Level.FINEST,"Common pool - setting targetSize to " + targetSize);
                }
            }
            if (targetSize < spliterator.estimateSize()) {
                Spliterator<T> tmp = spliterator.trySplit();
                if (tmp != null) {
                    return new BoundedSpliterator<>(tmp, targetSize);
                }
            }
            return null;
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
        public int characteristics() {
            return spliterator.characteristics();
        }

        @Override
        public boolean hasCharacteristics(int characteristics) {
            return spliterator.hasCharacteristics(characteristics);
        }

        @Override
        public Comparator<? super T> getComparator() {
            return spliterator.getComparator();
        }
    }
}
