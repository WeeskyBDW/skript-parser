package io.github.skriptlang.core.registry;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import io.github.skriptlang.core.lang.SyntaxElement;

/**
 * Contains information about a syntax element type.
 *
 */
public class SyntaxElementInfo {

	/**
	 * Class of the syntax element.
	 */
	private final Class<? extends SyntaxElement> type;
	
	private Supplier<? extends SyntaxElement> constructor;
	
	public SyntaxElementInfo(Class<? extends SyntaxElement> type) {
		this.type = type;
		setupConstructorLambda(MethodHandles.lookup());
	}
	
	/**
	 * Sets up a supplier for constructing instances of syntax element.
	 * @param lookup Lookup to use.
	 */
	private void setupConstructorLambda(MethodHandles.Lookup lookup) {
		try {
			MethodHandle mh = lookup.findConstructor(type, MethodType.methodType(void.class));
			constructor = (Supplier<? extends SyntaxElement>) LambdaMetafactory.metafactory(lookup, "get",
					MethodType.methodType(Supplier.class), mh.type(), mh, mh.type()).getTarget().invokeExact();
		} catch (Throwable e) {
			throw new AssertionError(e);
		}
	}
	
	/**
	 * Gets class of this syntax element.
	 * @return Class.
	 */
	public Class<? extends SyntaxElement> getType() {
		return type;
	}
	
	/**
	 * Creates a new instance of this syntax element. This may be faster than
	 * just using reflection to type of this.
	 * @return New instance.
	 */
	public SyntaxElement initialize() {
		return constructor.get();
	}
}
