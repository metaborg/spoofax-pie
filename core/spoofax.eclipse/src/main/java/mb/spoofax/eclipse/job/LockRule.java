package mb.spoofax.eclipse.job;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;

/**
 * Scheduling rule that mimics a lock.
 */
public class LockRule extends FakeMultiRule implements ISchedulingRule {
    private final String name;

    public LockRule(String name) {
        this.name = name;
    }

    public ReadLockRule createReadLock() {
        return new ReadLockRule(this, this.name + " read lock");
    }

    @Override public boolean isConflicting(ISchedulingRule rule) {
        if(this == rule) {
            return true;
        }
        if(rule instanceof MultiRule && !(rule instanceof FakeMultiRule)) {
            final MultiRule multi = (MultiRule)rule;
            for(ISchedulingRule child : multi.getChildren()) {
                if(isConflicting(child)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    @Override public boolean contains(ISchedulingRule rule) {
        if(this == rule) {
            return true;
        }
        if(rule instanceof MultiRule && !(rule instanceof FakeMultiRule)) {
            final MultiRule multi = (MultiRule)rule;
            for(ISchedulingRule child : multi.getChildren()) {
                if(!contains(child)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override public String toString() {
        return name;
    }
}
