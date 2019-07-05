package mb.spoofax.eclipse.expressions;

import mb.spoofax.eclipse.util.SelectionUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class SpoofaxPropertyTester extends PropertyTester {
    @Override
    public boolean test(@NonNull Object receiver, @NonNull String property, @NonNull Object[] args, @Nullable Object expectedValue) {
        switch(property) {
            case "hasNature":
                return hasNature(receiver, expectedValue != null ? expectedValue.toString() : "");
            case "isOpen":
                return isOpen(receiver);
        }
        return false;
    }


    private boolean hasNature(Object receiver, String expectedNature) {
        final @Nullable IProject project = SelectionUtils.elementToProject(receiver);
        if(project == null) {
            return false;
        }
        try {
            return project.hasNature(expectedNature);
        } catch(CoreException e) {
            return false;
        }
    }

    private boolean isOpen(Object receiver) {
        final @Nullable IProject project = SelectionUtils.elementToProject(receiver);
        if(project == null) {
            return false;
        }
        return project.isOpen();
    }
}
