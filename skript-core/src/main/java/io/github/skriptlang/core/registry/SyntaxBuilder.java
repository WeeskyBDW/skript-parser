package io.github.skriptlang.core.registry;

import io.github.skriptlang.core.lang.SyntaxElement;

public abstract class SyntaxBuilder<B extends SyntaxBuilder<B>> {
    
    /**
     * Where this builder will be {@link #register()}ed to.
     */
    protected final SyntaxRegistry registry;

    /**
     * Type of registered syntax.
     */
    protected final Class<? extends SyntaxElement> type;
    
    /**
     * Pattern of this expression. Note that pattern parsing is not part of
     * core; refer to parser module in regards to it.
     */
    private String pattern;
    
    public SyntaxBuilder(SyntaxRegistry registry, Class<? extends SyntaxElement> type) {
        assert registry != null : "null registry";
        assert type != null : "null type";
        this.registry = registry;
        this.type = type;
    }
    
    /**
     * Sets pattern of this syntax element. It will likely be used by parser,
     * if one is available in this environment.
     * @param pattern Syntax pattern.
     * @return This.
     */
    public B pattern(String pattern) {
        this.pattern = pattern;
        return self();
    }
    
    @SuppressWarnings("unchecked")
    private B self() {
        return (B) this;
    }
    
    public abstract void register();
}
