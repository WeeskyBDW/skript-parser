package io.github.skriptlang.core.lang;

import io.github.skriptlang.core.event.TriggerContext;
import io.github.skriptlang.core.log.SourceLine;

public class Trigger {

    /**
     * Starting line of the trigger.
     */
    private final SourceLine line;
    
    /**
     * Type of event that will cause this trigger to be fired.
     */
    private final Class<?> event;
    
    /**
     * All items in the trigger.
     */
    private TriggerItem[] items;
    
    public Trigger(SourceLine line, Class<?> event) {
        this.line = line;
        this.event = event;
    }
    
    /**
     * Gets starting line of this trigger.
     * @return Source line.
     */
    public SourceLine getLine() {
        return line;
    }
    
    /**
     * Gets type of event which will set this trigger off.
     * @return Event type.
     */
    public Class<?> getEvent() {
        return event;
    }
    
    public void setItems(TriggerItem[] items) {
        assert this.items == null : "items already set";
        this.items = items;
    }

    public TriggerItem[] getItems() {
        assert items != null : "items not yet set";
        return items;
    }
    
}
