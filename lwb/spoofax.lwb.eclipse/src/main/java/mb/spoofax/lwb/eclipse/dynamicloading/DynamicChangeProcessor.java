package mb.spoofax.lwb.eclipse.dynamicloading;

import mb.common.util.SetView;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.spoofax.lwb.dynamicloading.DynamicLanguage;
import mb.spoofax.lwb.dynamicloading.DynamicLanguageRegistry;
import mb.spoofax.lwb.dynamicloading.DynamicLanguageRegistryListener;
import mb.spoofax.lwb.eclipse.SpoofaxLwbScope;
import mb.spoofax.lwb.eclipse.util.EditorMappingUtils;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;

import javax.inject.Inject;

@SpoofaxLwbScope
public class DynamicChangeProcessor implements DynamicLanguageRegistryListener, AutoCloseable {
    private final Logger logger;
    private final DynamicLanguageRegistry dynamicLanguageRegistry;
    private final DynamicEditorTracker editorTracker;

    private final IWorkspace workspace;
    private final IEditorRegistry eclipseEditorRegistry;


    @Inject
    public DynamicChangeProcessor(
        LoggerFactory loggerFactory,
        DynamicLanguageRegistry dynamicLanguageRegistry,
        DynamicEditorTracker editorTracker
    ) {
        this.logger = loggerFactory.create(getClass());
        this.dynamicLanguageRegistry = dynamicLanguageRegistry;
        this.editorTracker = editorTracker;

        this.workspace = ResourcesPlugin.getWorkspace();
        this.eclipseEditorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
    }

    public void register() {
        dynamicLanguageRegistry.registerListener(this);
    }

    @Override public void close() {
        dynamicLanguageRegistry.unregisterListener(this);
    }


    @Override public void load(DynamicLanguage language, SetView<String> addedFileExtensions) {
        Display.getDefault().asyncExec(() -> {
            logger.trace("Adding '{}' editor mapping for file extensions '{}'", DynamicEditor.id, addedFileExtensions);
            EditorMappingUtils.set(eclipseEditorRegistry, DynamicEditor.id, addedFileExtensions);
        });

        for(DynamicEditor editor : editorTracker.getEditors()) {
            if(editor.getFileExtension() != null && addedFileExtensions.contains(editor.getFileExtension())) {
                editor.reconfigure();
            }
        }
    }

    @Override public void reload(
        DynamicLanguage previousLanguage,
        DynamicLanguage language,
        SetView<String> removedFileExtensions,
        SetView<String> addedFileExtensions
    ) {
        Display.getDefault().asyncExec(() -> {
            logger.trace("Removing '{}' editor mapping for file extensions '{}'", DynamicEditor.id, removedFileExtensions);
            EditorMappingUtils.remove(eclipseEditorRegistry, DynamicEditor.id, removedFileExtensions);
            logger.trace("Adding '{}' editor mapping for file extensions '{}'", DynamicEditor.id, addedFileExtensions);
            EditorMappingUtils.set(eclipseEditorRegistry, DynamicEditor.id, addedFileExtensions);
        });
        for(DynamicEditor editor : editorTracker.getEditors()) {
            if(language.getId().equals(editor.getLanguageId())) {
                if(editor.getFileExtension() != null && removedFileExtensions.contains(editor.getFileExtension())) {
                    editor.disable();
                } else {
                    editor.reconfigure();
                }
            }
        }
        // TODO: remove markers for files with removed file extension.
    }

    @Override
    public void unload(DynamicLanguage language, SetView<String> removedFileExtensions) {
        Display.getDefault().asyncExec(() -> {
            logger.trace("Removing '{}' editor mapping for file extensions '{}'", DynamicEditor.id, removedFileExtensions);
            EditorMappingUtils.remove(eclipseEditorRegistry, DynamicEditor.id, removedFileExtensions);
        });

        for(DynamicEditor editor : editorTracker.getEditors()) {
            if(editor.getFileExtension() != null && removedFileExtensions.contains(editor.getFileExtension())) {
                editor.disable();
            }
        }
        // TODO: remove markers for files with removed file extension.
    }
}
