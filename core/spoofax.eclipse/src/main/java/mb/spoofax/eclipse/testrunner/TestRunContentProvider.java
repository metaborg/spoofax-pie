package mb.spoofax.eclipse.testrunner;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jface.viewers.ITreeContentProvider;
import mb.spoofax.core.language.testrunner.MultiTestSuiteRun;
import mb.spoofax.core.language.testrunner.TestCaseRun;
import mb.spoofax.core.language.testrunner.TestSuiteRun;

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

    @Override @Nullable public Object getParent(Object element) {
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
