package mb.spoofax.eclipse.job;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;

/**
 * Scheduling rule that mimics a read lock of a read/write lock. For this to really act as a read lock, a new instance
 * of this lock must be created every time it is used.
 */
public class ReadLockRule extends FakeMultiRule implements ISchedulingRule {
    private final LockRule writeLock;
    private final String name;

    public ReadLockRule(LockRule writeLock, String name) {
        this.writeLock = writeLock;
        this.name = name;
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
        return rule == writeLock;
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
