/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.worldgen.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.annotation.Nonnull;

public class ListPool<T> {
    private final int capacity;
    private final T[] empty;
    private final ConcurrentLinkedQueue<Resource<T>> pool = new ConcurrentLinkedQueue();

    public ListPool(int capacity, T[] empty) {
        this.capacity = capacity;
        this.empty = empty;
        for (int i = 0; i < capacity; ++i) {
            this.pool.add(new Resource(this));
        }
    }

    public T[] emptyArray() {
        return this.empty;
    }

    @Nonnull
    public Resource<T> acquire() {
        Resource<T> resource = this.pool.poll();
        if (resource == null) {
            return new Resource(this);
        }
        return resource;
    }

    @Nonnull
    public Resource<T> acquire(int capacity) {
        Resource<T> resource = this.pool.poll();
        if (resource == null) {
            resource = new Resource(this);
        }
        resource.ensureCapacity(capacity);
        return resource;
    }

    public void release(@Nonnull Resource<T> resource) {
        if (this.pool.size() < this.capacity) {
            resource.clear();
            this.pool.offer(resource);
        }
    }

    public static class Resource<T>
    extends ObjectArrayList<T>
    implements AutoCloseable {
        private final ListPool<T> pool;

        public Resource(ListPool<T> pool) {
            this.pool = pool;
        }

        @Override
        @Nonnull
        public T[] toArray() {
            return super.toArray(this.pool.empty);
        }

        @Override
        public void close() {
            this.pool.release(this);
        }
    }
}

