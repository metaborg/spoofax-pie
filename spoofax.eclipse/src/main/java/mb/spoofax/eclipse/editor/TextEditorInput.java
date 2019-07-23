package mb.spoofax.eclipse.editor;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

import java.util.Objects;

public class TextEditorInput extends PlatformObject implements IStorageEditorInput {
    private final TextStorage textStorage;

    TextEditorInput(String name) {
        this.textStorage = new TextStorage(name);
    }

    @Override public TextStorage getStorage() {
        return textStorage;
    }

    @Override public boolean exists() {
        return true;
    }

    @Override public @Nullable ImageDescriptor getImageDescriptor() {
        return null;
    }

    @Override public String getName() {
        return textStorage.getName();
    }

    @Override public @Nullable IPersistableElement getPersistable() {
        return null;
    }

    @Override public String getToolTipText() {
        return "";
    }


    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final TextEditorInput other = (TextEditorInput) obj;
        return textStorage.equals(other.textStorage);
    }

    @Override public int hashCode() {
        return Objects.hash(textStorage);
    }

    @Override public String toString() {
        return textStorage.toString();
    }
}
