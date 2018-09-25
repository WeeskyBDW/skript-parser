package io.github.skriptlang.core.lang;

import org.jetbrains.annotations.Nullable;

import io.github.skriptlang.core.event.TriggerContext;

public interface SyntaxElement {
   
    void init(InitContext ctx);

    String toString(@Nullable TriggerContext e, boolean debug);
}
