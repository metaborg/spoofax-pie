package mb.spoofax.lwb.eclipse.util;

import com.google.common.collect.Iterables;
import mb.common.util.ArrayUtil;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.registry.FileEditorMapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Utility functions for changing editor mappings/associations.
 *
 * Note: Eclipse API expects {@link EditorRegistry}, {@link EditorDescriptor}, and {@link FileEditorMapping} instead of
 * their interface counterparts when changing mappings. Need to cast and use non-API classes to programmatically change
 * mappings.
 */
public class EditorMappingUtils {
    public static void set(IEditorRegistry iEditorRegistry, String editorId, Iterable<String> iterExtensions) {
        final EditorRegistry editorRegistry = (EditorRegistry)iEditorRegistry;
        final EditorDescriptor editorDescription = (EditorDescriptor)editorRegistry.findEditor(editorId);
        final String[] extensions = Iterables.toArray(iterExtensions, String.class);
        if(extensions.length == 0) {
            return;
        }
        final FileEditorMapping[] additionalMappings = new FileEditorMapping[extensions.length];
        for(int i = 0; i < extensions.length; ++i) {
            additionalMappings[i] = mapping(extensions[i], editorDescription);
        }
        final IFileEditorMapping[] iMappings = editorRegistry.getFileEditorMappings();
        final FileEditorMapping[] mappings = Arrays.copyOf(iMappings, iMappings.length, FileEditorMapping[].class);
        final FileEditorMapping[] newMappings = ArrayUtil.addAll(mappings, additionalMappings);
        editorRegistry.setFileEditorMappings(newMappings);
    }

    public static void remove(IEditorRegistry iEditorRegistry, String editorId, Iterable<String> iterExtensions) {
        final EditorRegistry editorRegistry = (EditorRegistry)iEditorRegistry;
        final EditorDescriptor editorDescription = (EditorDescriptor)editorRegistry.findEditor(editorId);
        final String[] extensions = Iterables.toArray(iterExtensions, String.class);
        if(extensions.length == 0) {
            return;
        }
        final IFileEditorMapping[] mappings = editorRegistry.getFileEditorMappings();
        for(final IFileEditorMapping iFileEditorMapping : mappings) {
            final FileEditorMapping mapping = (FileEditorMapping)iFileEditorMapping;
            if(ArrayUtil.contains(extensions, mapping.getExtension())) {
                mapping.removeEditor(editorDescription);
            }
        }
        final FileEditorMapping[] newMappings = Arrays.copyOf(mappings, mappings.length, FileEditorMapping[].class);
        editorRegistry.setFileEditorMappings(newMappings);
    }


    private static FileEditorMapping mapping(String extension, EditorDescriptor editorDescription) {
        final FileEditorMapping mapping = new FileEditorMapping(extension);
        mapping.addEditor(editorDescription);
        setDefaultEditor(mapping, editorDescription);
        return mapping;
    }

    /**
     * Sets the default editor for given mapping. Compatible with Eclipse Mars and previous versions via reflection.
     *
     * @param mapping           Mapping to set default editor for.
     * @param editorDescription Default editor to set.
     */
    public static void setDefaultEditor(FileEditorMapping mapping, EditorDescriptor editorDescription) {
        final Method method = getSetDefaultEditorMethod();
        try {
            method.invoke(mapping, editorDescription);
        } catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException("Could not set default editor", e);
        }
    }

    /**
     * Gets the correct {@link FileEditorMapping#setDefaultEditor} method via reflection. Parameter changed from
     * EditorDescriptor to IEditorDescriptor in Eclipse Mars.
     */
    private static Method getSetDefaultEditorMethod() {
        final String methodName = "setDefaultEditor";
        try {
            // noinspection JavaReflectionMemberAccess (can be resolved in older versions of Eclipse)
            return FileEditorMapping.class.getDeclaredMethod(methodName, EditorDescriptor.class);
        } catch(NoSuchMethodException e1) {
            try {
                return FileEditorMapping.class.getDeclaredMethod(methodName, IEditorDescriptor.class);
            } catch(NoSuchMethodException e2) {
                throw new RuntimeException("Cannot find setDefaultEditor method via reflection", e2);
            }
        }
    }
}
