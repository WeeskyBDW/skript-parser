package io.github.skriptlang.core.lang;

import io.github.skriptlang.core.event.TriggerContext;

public interface SingularExpression<T> extends Expression<T> {
    
    @Override
    @SuppressWarnings("unchecked")
    default T[] getAll(TriggerContext ctx) {
        return (T[]) new Object[] {getOne(ctx)}; // Just box what we're returning
    }
    
    @Override
    default boolean isSingle() {
        return true;
    }
    
    @Override
    default void setAll(TriggerContext ctx, T[] values) {
        assert values.length == 1;
        setOne(ctx, values[0]); // Unbox the value from array
    }
}
