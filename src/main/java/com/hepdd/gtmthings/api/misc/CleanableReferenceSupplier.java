package com.hepdd.gtmthings.api.misc;

import java.lang.ref.WeakReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public class CleanableReferenceSupplier<T> implements Supplier<T> {

    private WeakReference<T> reference;

    private final Supplier<T> supplier;
    private final Predicate<T> removeCondition;

    public CleanableReferenceSupplier(Supplier<T> supplier, Predicate<T> removeCondition) {
        this.supplier = supplier;
        this.removeCondition = removeCondition;
    }

    public void clean() {
        reference = null;
    }

    @Override
    public @Nullable T get() {
        if (reference == null) {
            var value = supplier.get();
            if (value == null) return null;
            reference = new WeakReference<>(value);
            return value;
        }
        var value = reference.get();
        if (value != null) {
            if (removeCondition.test(value)) {
                reference = null;
            } else {
                return value;
            }
        }
        value = supplier.get();
        if (value == null) return null;
        reference = new WeakReference<>(value);
        return value;
    }
}
