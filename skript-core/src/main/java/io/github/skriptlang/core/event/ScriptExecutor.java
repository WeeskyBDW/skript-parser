package io.github.skriptlang.core.event;

public interface ScriptExecutor {

    /**
     * Runs the trigger, starting at first item.
     * 
     * <p>This method blocks the caller thread.
     */
    void run();
    
    /**
     * Interrupts execution of the trigger after this item.
     */
    void interrupt();
    
    /**
     * Resumes execution of the trigger. It must be interrupted for this
     * to work. While resuming execution in another thread is technically safe,
     * note that the executed code might might not be thread-safe.
     * 
     * <p>This method blocks the caller thread.
     */
    void resume();
    
    /**
     * Exits the trigger permanently.
     */
    void exit();
    
    /**
     * Performs a jump/goto relative to current position. Next executed trigger
     * item will be the one with given offset + 1.
     * @param offset Jump offset.
     */
    void jump(int offset);
}
