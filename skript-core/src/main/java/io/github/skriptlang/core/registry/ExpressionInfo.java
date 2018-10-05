package io.github.skriptlang.core.registry;

import io.github.skriptlang.core.lang.SyntaxElement;

public class ExpressionInfo extends SyntaxElementInfo {
	
	private final Class<?> returnType;

	public ExpressionInfo(Class<? extends SyntaxElement> type, Class<?> returnType) {
		super(type);
		this.returnType = returnType;
	}
	
	public Class<?> getReturnType() {
		return returnType;
	}
    
}
