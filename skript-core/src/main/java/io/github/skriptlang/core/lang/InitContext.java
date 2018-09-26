package io.github.skriptlang.core.lang;

import java.util.List;

import io.github.skriptlang.core.log.ErrorType;
import io.github.skriptlang.core.log.InitErrorException;
import io.github.skriptlang.core.log.SourceLine;

/**
 * Initialization context contains everything that a syntax element might need
 * to be initialized.
 *
 */
public class InitContext {
    
    // Expressions
    
    /**
     * All expressions; this includes some unrelated to this init context.
     */
    private final List<Expression<?>> allExprs;
    
    /**
     * Starting slot in all expressions for this context.
     */
    private final int startSlot;
    
    /**
     * When asserts are enabled, this array contains information about which
     * expressions have already been {@link #take(Class, int)}n.
     */
    private boolean[] used;
    
    // General information
    
    /**
     * Location of the line that caused this initialization.
     */
    private final SourceLine line;
    
    /**
     * Trigger inside which this is initializating.
     */
    private final Trigger trigger;
    
    public InitContext(List<Expression<?>> allExprs, int startSlot, SourceLine line, Trigger trigger) {
        assert allExprs != null;
        assert startSlot >= 0 && startSlot < allExprs.size();
        assert line != null;
        assert trigger != null;
        
        this.allExprs = allExprs;
        this.startSlot = startSlot;
        // If asserts are enabled, make an array for tracking already used slots
        assert (used = new boolean[allExprs.size() - startSlot]) != null;
        
        this.line = line;
        this.trigger = trigger;
    }
    
    /**
     * Peeks at an expression this syntax element has defined. The expression
     * can be peeked at again or be taken in future.
     * @param type Type of expression.
     * @param slot Slot of the expression.
     * @return The expression in question.
     */
    @SuppressWarnings("unchecked")
    public <T> Expression<T> peek(Class<T> type, int slot) {
        assert type != null : "null type";
        int index = startSlot + slot;
        assert index >= startSlot && index < allExprs.size() : "slot out of bounds";
        
        Expression<?> expr = allExprs.get(index);
        assert type.isAssignableFrom(expr.getReturnType()) : "wrong slot type";
        assert used[slot] : "slot already used";
        
        return (Expression<T>) expr;
    }
    
    /**
     * Takes an expression this syntax element has defined. It shouldn't be peeked
     * at or be taken again in future.
     * @param type Type of expression.
     * @param slot Slot of the expression.
     * @return The expression in question.
     */
    public <T> Expression<T> take(Class<T> type, int slot) {
        Expression<T> expr = peek(type, slot);
        assert (used[slot] = true); // If asserts are enabled, mark usage
        return expr;
    }
    
    /**
     * Gets location of the line that caused this initialization.
     * @return Source line information.
     */
    public SourceLine getLine() {
        return line;
    }
    
    public Trigger getTrigger() {
        return trigger;
    }
    
    public void error(String msg, ErrorType type) {
        throw new InitErrorException(msg, line, trigger, type);
    }
    
    public void warning(String msg) {
        // TODO implement warnings
    }
}
