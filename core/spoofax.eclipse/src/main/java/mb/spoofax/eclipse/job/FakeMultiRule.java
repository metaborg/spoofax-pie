package mb.spoofax.eclipse.job;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;

public class FakeMultiRule extends MultiRule {
    // HACK: extend MultiRule with empty list to make users of this rule compatible with Resource rules.
    public FakeMultiRule() {
        super(new ISchedulingRule[0]);
    }
}
