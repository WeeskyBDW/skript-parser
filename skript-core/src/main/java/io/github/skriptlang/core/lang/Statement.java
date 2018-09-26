package io.github.skriptlang.core.lang;

import io.github.skriptlang.core.event.TriggerContext;

public interface Statement extends SyntaxElement, TriggerItem {

    @Override
    default int walk(TriggerContext ctx) {
        run(ctx);
        return 1;
    }
    
    void run(TriggerContext ctx);
}
