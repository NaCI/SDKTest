package com.netmera.internal;

import androidx.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

final class Present<T> extends Optional<T> {
    private final T reference;
    private static final long serialVersionUID = 0L;

    Present(T reference) {
        this.reference = reference;
    }

    public boolean isPresent() {
        return true;
    }

    public T get() {
        return this.reference;
    }

    public T or(T defaultValue) {
        Preconditions.checkNotNull(defaultValue, "use Optional.orNull() instead of Optional.or(null)");
        return this.reference;
    }

    public Optional<T> or(Optional<? extends T> secondChoice) {
        Preconditions.checkNotNull(secondChoice);
        return this;
    }

    public T orNull() {
        return this.reference;
    }

    public Set<T> asSet() {
        return Collections.singleton(this.reference);
    }

    public boolean equals(@Nullable Object object) {
        if (object instanceof Present) {
            Present<?> other = (Present)object;
            return this.reference.equals(other.reference);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return 1502476572 + this.reference.hashCode();
    }

    public String toString() {
        return "Optional.of(" + this.reference + ")";
    }
}
