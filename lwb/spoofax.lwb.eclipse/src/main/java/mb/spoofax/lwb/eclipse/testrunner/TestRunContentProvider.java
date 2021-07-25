package mb.spoofax.lwb.eclipse.testrunner;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import mb.spt.model.MultiTestSuiteRun;
import mb.spt.model.TestCaseRun;
import mb.spt.model.TestSuiteRun;

/**
 * A content provider to turn our data model into something the treeviewer can understand.
 */
public class TestRunContentProvider implements ITreeContentProvider {

    @Override public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    @Override public Object[] getChildren(Object parentElement) {
        if(parentElement instanceof TestSuiteRun) {
            return ((TestSuiteRun) parentElement).tests.toArray();
        } else if(parentElement instanceof MultiTestSuiteRun) {
            return ((MultiTestSuiteRun) parentElement).suites.toArray();
        }
        return new Object[] {};
    }

    @Override public Object getParent(Object element) {
        if(element instanceof TestCaseRun) {
            return ((TestCaseRun) element).parent;
        } else if(element instanceof TestSuiteRun) {
            return ((TestSuiteRun) element).parent;
        }
        return null;
    }

    @Override public boolean hasChildren(Object element) {
        return getChildren(element).length > 0;
    }

}
