package io.github.skriptlang.core.lang;

import io.github.skriptlang.core.event.TriggerContext;
import io.github.skriptlang.core.lang.annotation.Literal;
import io.github.skriptlang.core.lang.annotation.ReturnType;

public interface Expression<T> extends SyntaxElement {

    /**
     * Gets one value for this expression from the given context.
     * If there are many many values available, an exception will be thrown
     * instead; don't use this without checking {@link #isSingle()} first!
     * @param ctx Trigger context.
     * @return A single value from the expression
     */
    T getOne(TriggerContext ctx);
    
    /**
     * Gets all values for this expression from the given context.
     * Using this for expressions that only return single values is allowed,
     * but may cause performance issues and certainly produces garbage.
     * @param ctx Trigger context.
     * @return All values from the expression.
     */
    T[] getAll(TriggerContext ctx);
    
    /**
     * Checks if this expression will always return only one value.
     * Such an expression can have {@link #getOne(TriggerContext)}
     * called at it, which improves performance.
     * @return Whether this expression always returns one value.
     */
    boolean isSingle();
    
    /**
     * Sets one value to this expression in given context. Checking
     * {@link #isSingle()} is not needed; the value will just be automatically
     * boxed into an array.
     * @param ctx Trigger context.
     * @param value The value to set this to.
     */
    void setOne(TriggerContext ctx, T value);
    
    /**
     * Sets all values to this expression in given context. If this cannot take
     * many values, and more than one value was given, an exception will be
     * thrown.
     * @param ctx Trigger context.
     * @param values Values to set this to.
     */
    void setAll(TriggerContext ctx, T[] values);
    
    /**
     * Gets the most specific return type known for this expression
     * compile-time.
     * @return Return type known for this expression.
     */
    @SuppressWarnings("unchecked")
    default Class<T> getReturnType() {
        ReturnType type = getClass().getDeclaredAnnotation(ReturnType.class);
        assert type != null : "return type annotation missing and method not overridden";
        return (Class<T>) type.value(); // TODO generics?
    }

}
