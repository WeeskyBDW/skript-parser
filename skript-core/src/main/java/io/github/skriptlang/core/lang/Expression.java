package io.github.skriptlang.core.lang;

import io.github.skriptlang.core.event.TriggerContext;

public interface Expression<T> extends SyntaxElement {

    T getOne(TriggerContext ctx);
    
    T[] getAll(TriggerContext ctx);
    
    void setOne(TriggerContext ctx, T value);
    
    void setManu(TriggerContext ctx, T[] values);
    
    boolean isMany();
    
    Class<T> getReturnType();
    
    default boolean isLiteral() {
        return false;
    }
}
