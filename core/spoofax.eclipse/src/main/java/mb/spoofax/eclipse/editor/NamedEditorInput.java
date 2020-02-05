package mb.spoofax.eclipse.editor;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import java.util.Objects;

public class NamedEditorInput extends PlatformObject implements IEditorInput {
    private final String name;

    public NamedEditorInput(String name) {
        this.name = name;
    }

    @Override public boolean exists() {
        return false; // False so it does not show up in most recently used file list.
    }

    @Override public @Nullable ImageDescriptor getImageDescriptor() {
        return null;
    }

    @Override public String getName() {
        return name;
    }

    @Override public @Nullable IPersistableElement getPersistable() {
        return null;
    }

    @Override public String getToolTipText() {
        return name;
    }

    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final NamedEditorInput other = (NamedEditorInput) obj;
        return name.equals(other.name);
    }

    @Override public int hashCode() {
        return Objects.hash(name);
    }

    @Override public String toString() {
        return name;
    }
}
