package io.github.skriptlang.core.lang;

import io.github.skriptlang.core.event.TriggerContext;

public interface PluralExpression<T> extends Expression<T> {
    
    @Override
    default T getOne(TriggerContext ctx) {
        throw new AssertionError("tried to get one value from plural expression");
    }
    
    @Override
    default boolean isSingle() {
        return false;
    }
    
    @SuppressWarnings("unchecked")
    default void setOne(TriggerContext ctx, T value) {
        setAll(ctx, (T[]) new Object[] {value}); // Box our value to an array
    }
}
