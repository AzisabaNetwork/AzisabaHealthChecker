package net.azisaba.healthChecker.util;

import java.util.Objects;

public class Tuple<A, Z, I> {
    private final A first;
    private final Z second;
    private final I third;

    public Tuple(A first, Z second, I third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public A getFirst() {
        return first;
    }

    public Z getSecond() {
        return second;
    }

    public I getThird() {
        return third;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple)) return false;
        Tuple<?, ?, ?> tuple = (Tuple<?, ?, ?>) o;
        return Objects.equals(first, tuple.first) && Objects.equals(second, tuple.second) && Objects.equals(third, tuple.third);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second, third);
    }
}
