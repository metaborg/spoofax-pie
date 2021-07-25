package mb.spoofax.lwb.eclipse.testrunner;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import mb.spt.model.TestCaseRun;
import mb.spt.model.TestSuiteRun;

/**
 * Returns the labels for the suites and test cases.
 */
public class TestRunLabelProvider extends LabelProvider
    implements ITableLabelProvider, ITableFontProvider, ITableColorProvider {

    @Override public String getText(Object element) {
        if(element instanceof TestSuiteRun) {
            TestSuiteRun tsr = (TestSuiteRun) element;
            int failed = tsr.numFailed();
            return failed == 0 ? tsr.name : String.format("%s (%d failed)", tsr.name, failed);
        } else if(element instanceof TestCaseRun) {
            TestCaseRun tcr = (TestCaseRun) element;
            String lbl = tcr.test.description;
            if(tcr.result() != null) {
                lbl = lbl + " (" + String.format("%.2f", tcr.duration() / 1000.0) + "s)";
                if(tcr.result().containsError()) {
                    lbl += " : FAILED";
                }
            }
            return lbl;
        }
        return super.getText(element);
    }

    @Override public Color getForeground(Object element, int columnIndex) {
        if(element instanceof TestSuiteRun) {
            TestSuiteRun tsr = (TestSuiteRun) element;
            if(tsr.messages != null && !tsr.messages.containsError()) {
                // use default color;
                return null;
            } else {
                // use red
                return new Color(Display.getCurrent(), 159, 63, 63);
            }
        } else if(element instanceof TestCaseRun) {
            TestCaseRun tcr = (TestCaseRun) element;
            if(tcr.result() != null) {
                return !tcr.result().containsError() ? new Color(Display.getCurrent(), 10, 100, 10)
                    : new Color(Display.getCurrent(), 159, 63, 63);
            }
        }
        return null;
    }

    @Override public Color getBackground(Object element, int columnIndex) {
        return null;
    }

    @Override public Font getFont(Object element, int columnIndex) {
        return null;
    }

    @Override public Image getColumnImage(Object element, int columnIndex) {
        return getImage(element);
    }

    @Override public String getColumnText(Object element, int columnIndex) {
        return getText(element);
    }

}
