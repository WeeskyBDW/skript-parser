package io.github.skriptlang.core.registry;

import io.github.skriptlang.core.lang.SyntaxElement;

public class ExpressionBuilder extends SyntaxBuilder<ExpressionBuilder> {

    /**
     * Whether this is known compile time or not.
     */
    private boolean isLiteral;
    
    public ExpressionBuilder(SyntaxRegistry registry, Class<? extends SyntaxElement> type) {
        super(registry, type);
    }
    
    /**
     * Sets whether this expression is a literal (known compile-time)
     * or not. By default, expressions are not literals. If type of this is
     * annotated with {@link Literal}, value of it will be used instead.
     * @param literal Whether this is a literal or not.
     * @return This.
     */
    public ExpressionBuilder literal(boolean literal) {
        this.isLiteral = literal;
        return this;
    }

    @Override
    public void register() {
        // TODO
    }
}
