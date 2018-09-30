package io.github.skriptlang.core.interpreter;

import io.github.skriptlang.core.event.ScriptExecutor;
import io.github.skriptlang.core.event.TriggerContext;
import io.github.skriptlang.core.lang.TriggerItem;

/**
 * A simple interpreter to execute scripts. A lot of safety checks are
 * implemented using asserts; use them when debugging.
 *
 */
public class InterpreterExecutor implements ScriptExecutor {
    
    private TriggerContext ctx;
    private TriggerItem[] items;
    private int resumeIndex;
    
    private boolean started;
    private boolean finished;
    
    private boolean check;
    private boolean interrupted;
    private int jumpOffset;
    
    public InterpreterExecutor(TriggerContext ctx, TriggerItem[] items) {
        this.ctx = ctx;
        this.items = items;
        this.resumeIndex = 0;
        this.started = false;
        this.finished = false;
        this.check = false;
        this.interrupted = false;
        this.jumpOffset = 0;
    }

    private void execute() {
        TriggerContext ctx = this.ctx;
        TriggerItem[] items = this.items;
        
        int i = resumeIndex; // Correctly resume from an interrupt
        if (check) { // First check before first item
            check = false; // And not after first item (unless requested again)
            i += jumpOffset; // Modify position by jump offset
            jumpOffset = 0; // Reset jump offset (for next time)
            interrupted = false; // Resume from an interrupt
        }
        
        for (; i < items.length; i++) {
            items[i].walk(ctx, this);
            if (check) {
                check = false; // Don't check again unless asked
                i += jumpOffset; // Modify position by jump offset
                jumpOffset = 0; // Reset jump offset (for next time)
                if (interrupted) {
                    resumeIndex = i; // Save index to use for resuming
                    return;
                }
            }
        }
        
        // Finished, since we ran out of items
        finished = true;
    }
    
    @Override
    public void run() {
        assert !started : "run() called twice (did you mean resume()?)";
        started = true;
        execute();
    }

    @Override
    public void interrupt() {
        assert !finished && started : "not executing, can't interrupt";
        check = true;
        interrupted = true;
    }

    @Override
    public void resume() {
        assert started : "resume() called before run()";
        assert !finished : "already exited";
        execute();
    }

    @Override
    public void exit() {
        assert started : "exit() called before run()";
        assert !finished : "exit() called twice";
        finished = true; // We're done!
        interrupt(); // Interrupt to actually exit
    }

    @Override
    public void jump(int offset) {
        // Jumping before run():ing is allowed
        assert !finished : "already exited, jumping around is useless now";
        check = true;
        jumpOffset = offset;
    }
}
