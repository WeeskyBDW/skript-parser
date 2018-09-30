package io.github.skriptlang.core.lang;

import io.github.skriptlang.core.event.ScriptExecutor;
import io.github.skriptlang.core.event.TriggerContext;

public interface Statement extends SyntaxElement, TriggerItem {

    @Override
    default void walk(TriggerContext ctx, ScriptExecutor executor) {
        run(ctx);
    }
    
    void run(TriggerContext ctx);
}
