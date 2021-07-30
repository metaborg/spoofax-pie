package mb.spoofax.eclipse.testrunner;

import mb.common.message.KeyedMessages;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import mb.spoofax.core.language.testrunner.TestCaseRun;
import mb.spoofax.core.language.testrunner.TestSuiteRun;

public class FailedTestsFilter extends ViewerFilter {

    @Override public boolean select(Viewer viewer, Object parentElement, Object element) {
        if(element instanceof TestCaseRun) {
            @Nullable KeyedMessages r = ((TestCaseRun) element).result();
            return r != null && r.containsError();
        } else if(element instanceof TestSuiteRun) {
            return ((TestSuiteRun) element).numFailed() > 0;
        }
        return true;
    }

}
