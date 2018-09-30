package io.github.skriptlang.core.lang;

import io.github.skriptlang.core.event.ScriptExecutor;
import io.github.skriptlang.core.event.TriggerContext;

public interface Scope extends Statement {
    
    class ScopeEnd implements TriggerItem {
        
        private Scope scope;
        
        public ScopeEnd(Scope scope) {
            this.scope = scope;
        }
        
        @Override
        public void walk(TriggerContext ctx, ScriptExecutor executor) {
            scope.leave(ctx);
        }
    }

    @Override
    default void walk(TriggerContext ctx, ScriptExecutor executor) {
        enter(ctx);
    }
    
    void enter(TriggerContext ctx);
    
    void leave(TriggerContext ctx);
}
