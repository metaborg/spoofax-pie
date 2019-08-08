package mb.spoofax.cli;

import org.checkerframework.checker.nullness.qual.Nullable;

class DelegateRunnable implements Runnable {
    private @Nullable Runnable runnable = null;

    void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override public void run() {
        if(runnable != null) {
            runnable.run();
        }
    }
}
