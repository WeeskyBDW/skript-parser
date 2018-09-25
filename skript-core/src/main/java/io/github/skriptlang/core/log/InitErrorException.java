package io.github.skriptlang.core.log;

import io.github.skriptlang.core.lang.Trigger;

@SuppressWarnings("serial")
public class InitErrorException extends RuntimeException {

    /**
     * Line where this error occurred.
     */
    private final SourceLine line;
    
    /**
     * Trigger inside which this error occurred.
     */
    private final Trigger parent;
    
    /**
     * Type of the error.
     */
    private final ErrorType type;
    
    public InitErrorException(String msg, SourceLine line, Trigger parent, ErrorType type) {
        super(msg);
        this.line = line;
        this.parent = parent;
        this.type = type;
    }
    
    public SourceLine getLine() {
        return line;
    }
    
    public Trigger getParent() {
        return parent;
    }
    
    public ErrorType getType() {
        return type;
    }
    
    public String toMessage() {
        return getMessage() + "(at " + getLine() + ")"; // TODO debug mode -> more information?
    }
}
