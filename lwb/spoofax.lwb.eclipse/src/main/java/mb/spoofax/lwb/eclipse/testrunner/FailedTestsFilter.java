package mb.spoofax.lwb.eclipse.testrunner;

import mb.common.message.KeyedMessages;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import mb.spt.model.TestCaseRun;
import mb.spt.model.TestSuiteRun;

import javax.annotation.Nullable;

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
