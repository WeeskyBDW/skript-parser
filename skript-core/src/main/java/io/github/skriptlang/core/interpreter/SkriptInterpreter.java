package io.github.skriptlang.core.interpreter;

import io.github.skriptlang.core.event.TriggerContext;
import io.github.skriptlang.core.lang.Trigger;
import io.github.skriptlang.core.lang.TriggerItem;

public class SkriptInterpreter {

    public void run(Trigger trigger, TriggerContext ctx) {
        TriggerItem[] items = trigger.getItems(); // Get contents of the trigger
        assert items != null;
        for (int i = 0; i < items.length; i++) {
            items[i].walk(ctx, null); // TODO executor creation
        }
    }
}
