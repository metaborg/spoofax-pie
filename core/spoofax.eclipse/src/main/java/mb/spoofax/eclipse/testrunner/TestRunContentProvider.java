package mb.spoofax.eclipse.testrunner;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jface.viewers.ITreeContentProvider;
import mb.spoofax.core.language.testrunner.TestResults;
import mb.spoofax.core.language.testrunner.TestCaseResult;
import mb.spoofax.core.language.testrunner.TestSuiteResult;

/**
 * A content provider to turn our data model into something the treeviewer can understand.
 */
public class TestRunContentProvider implements ITreeContentProvider {

    @Override public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    @Override public Object[] getChildren(Object parentElement) {
        if(parentElement instanceof TestSuiteResult) {
            return ((TestSuiteResult) parentElement).tests.toArray();
        } else if(parentElement instanceof TestResults) {
            return ((TestResults) parentElement).suites.toArray();
        }
        return new Object[] {};
    }

    @Override @Nullable public Object getParent(Object element) {
        if(element instanceof TestCaseResult) {
            return ((TestCaseResult) element).parent;
        } else if(element instanceof TestSuiteResult) {
            return ((TestSuiteResult) element).parent;
        }
        return null;
    }

    @Override public boolean hasChildren(Object element) {
        return getChildren(element).length > 0;
    }

}
