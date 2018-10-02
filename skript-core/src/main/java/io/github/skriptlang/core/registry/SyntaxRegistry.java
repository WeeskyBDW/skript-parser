package io.github.skriptlang.core.registry;

import java.util.List;

import io.github.skriptlang.core.lang.Expression;

public class SyntaxRegistry {

    /**
     * Whether syntaxes can be gotten from this registry at the moment.
     */
    private boolean compiled;
    
    private List<ExpressionInfo> expressions;
    
    public ExpressionBuilder expression(Class<? extends Expression<?>> type) {
        return new ExpressionBuilder(this, type);
    }
    
    public void compile() {
        
    }
}
