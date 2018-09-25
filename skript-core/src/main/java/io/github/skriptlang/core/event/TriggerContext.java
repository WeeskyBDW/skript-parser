package io.github.skriptlang.core.event;

public interface TriggerContext {
    TriggerContext DUMMY = () -> "dummy";

    String getName();
}
