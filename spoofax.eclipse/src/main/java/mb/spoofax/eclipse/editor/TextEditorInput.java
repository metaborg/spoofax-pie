package mb.spoofax.eclipse.editor;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

public class TextEditorInput extends PlatformObject implements IStorageEditorInput {
    private final TextStorage textStorage;


    public TextEditorInput(String text, String name) {
        this.textStorage = new TextStorage(text, name);
    }


    @Override public IStorage getStorage() throws CoreException {
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
}
