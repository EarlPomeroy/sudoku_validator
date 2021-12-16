package org.kiva.common.utils;

import java.util.Objects;

/**
 * Holds an immutable pair of values
 *
 * @param <L> Left value of type L
 * @param <R> Right value of type R
 */
public class Pair<L, R> {
    private final L l;
    private final R r;

    public Pair(L left, R right) {
        this.l = left;
        this.r = right;
    }

    public L getLeft() {
        return l;
    }

    public R getRight() {
        return r;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(l, pair.l) && Objects.equals(r, pair.r);
    }

    @Override
    public int hashCode() {
        return Objects.hash(l, r);
    }
}
