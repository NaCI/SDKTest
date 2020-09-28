package com.netmera.internal;

import androidx.annotation.Nullable;
import java.io.Serializable;
import java.util.Set;

public abstract class Optional<T> implements Serializable {
    private static final long serialVersionUID = 0L;

    public static <T> Optional<T> absent() {
        return Absent.withType();
    }

    public static <T> Optional<T> of(T reference) {
        return new Present(Preconditions.checkNotNull(reference));
    }

    public static <T> Optional<T> fromNullable(@Nullable T nullableReference) {
        return (Optional)(nullableReference == null ? absent() : new Present(nullableReference));
    }

    Optional() {
    }

    public abstract boolean isPresent();

    public abstract T get();

    public abstract T or(T var1);

    public abstract Optional<T> or(Optional<? extends T> var1);

    @Nullable
    public abstract T orNull();

    public abstract Set<T> asSet();

    public abstract boolean equals(@Nullable Object var1);

    public abstract int hashCode();

    public abstract String toString();
}
