package io.github.skriptlang.core.log;

public final class ErrorType {

    /**
     * The script could be loaded, but it would likely do something that the
     * programmer had not intended. The programmer should clarify what they're
     * meaning (for example, perform a manual type cast).
     */
    public static final ErrorType SEMANTIC_ERROR = new ErrorType("SEMANTIC_ERROR");
    
    private final String id;
    
    public ErrorType(String id) {
        this.id = id;
    }
    
    public String toString() {
        return id;
    }
}
