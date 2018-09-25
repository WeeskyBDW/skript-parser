package io.github.skriptlang.core.log;

/**
 * Represents a single line somewhere.
 *
 */
public class SourceLine {
    
    private final String content;

    private final String source;
        
    private final int line;
    
    public SourceLine(String content, String source, int line) {
        this.content = content;
        this.source = source;
        this.line = line;
    }
    
    public String getContent() {
        return content;
    }
    
    public String getSource() {
        return source;
    }
    
    public int getLine() {
        return line;
    }
    
    public String toString() {
        return source + ", line " + line; // TODO debug mode
    }
    
}
