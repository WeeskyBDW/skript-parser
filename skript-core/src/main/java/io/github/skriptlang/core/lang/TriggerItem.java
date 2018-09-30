package io.github.skriptlang.core.lang;

import io.github.skriptlang.core.event.ScriptExecutor;
import io.github.skriptlang.core.event.TriggerContext;

public interface TriggerItem {

    void walk(TriggerContext ctx, ScriptExecutor executor);
}
