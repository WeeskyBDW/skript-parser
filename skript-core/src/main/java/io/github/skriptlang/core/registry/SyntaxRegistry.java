package io.github.skriptlang.core.registry;

import java.util.List;

import io.github.skriptlang.core.lang.Expression;

public class SyntaxRegistry {
    
    private List<ExpressionInfo> expressions;
    
    public ExpressionBuilder expression(Class<? extends Expression<?>> type) {
        return new ExpressionBuilder(this, type);
    }
    
}
