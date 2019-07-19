package mb.spoofax.eclipse.transform;

import mb.common.message.Messages;
import mb.common.region.Region;
import mb.common.util.MapView;
import mb.common.util.SerializationUtil;
import mb.pie.api.ExecException;
import mb.pie.api.PieSession;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.spoofax.core.language.transform.*;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.editor.TextEditorInput;
import mb.spoofax.eclipse.resource.WrapsEclipseResource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import java.util.Base64;
import java.util.HashMap;

public class TransformHandler extends AbstractHandler {
    public final static String dataParameterId = "data";

    private final EclipseLanguageComponent languageComponent;

    private final ResourceService resourceService;

    private final MapView<String, TransformDef> transformDefsPerId;


    public TransformHandler(EclipseLanguageComponent languageComponent) {
        this.languageComponent = languageComponent;

        this.resourceService = SpoofaxPlugin.getComponent().getResourceService();

        final HashMap<String, TransformDef> transformDefsPerId = new HashMap<>();
        for(TransformDef transformDef : languageComponent.getLanguageInstance().getTransformDefs()) {
            transformDefsPerId.put(transformDef.getId(), transformDef);
        }
        this.transformDefsPerId = new MapView<>(transformDefsPerId);
    }


    @Override public @Nullable Object execute(ExecutionEvent event) throws ExecutionException {
        final @Nullable String dataStr = event.getParameter(dataParameterId);
        if(dataStr == null) {
            throw new ExecutionException("Cannot execute transform, no argument for '" + dataParameterId + "' parameter was set");
        }
        final byte[] decoded = Base64.getDecoder().decode(dataStr);
        final TransformData data = SerializationUtil.deserialize(decoded, TransformHandler.class.getClassLoader());
        final @Nullable TransformDef def = transformDefsPerId.get(data.transformId);
        if(def == null) {
            throw new ExecutionException("Cannot execute transform with ID '" + data.transformId + "', transform with that ID was not found in language '" + languageComponent.getLanguageInstance().getDisplayName() + "'");
        }
        try(final PieSession session = languageComponent.newPieSession()) {
            switch(data.executionType) {
                case OneShot:
                    for(TransformInput input : data.inputs) {
                        final TransformOutput output = session.requireWithoutObserving(def.createTask(input));
                        processOutput(output);
                    }
                    break;
                case ContinuousOnResource:
                    for(TransformInput input : data.inputs) {
                        final TransformOutput output = session.require(def.createTask(input));
                        processOutput(output);
                    }
                    break;
                case ContinuousOnEditor:
                    for(TransformInput input : data.inputs) {
                        // TODO: continuous transform on editor requires special support.
                        final TransformOutput output = session.requireWithoutObserving(def.createTask(input));
                        processOutput(output);
                    }
                    break;
            }
        } catch(ExecException e) {
            throw new ExecutionException("Cannot execute transform '" + data.transformId + "', execution failed unexpectedly", e);
        }
        return null;
    }

    private void processOutput(TransformOutput output) {
        for(TransformFeedback feedback : output.feedback) {
            processFeedback(feedback);
        }
    }

    private void processFeedback(TransformFeedback feedback) {
        feedback.accept(new TransformFeedbackVisitor() {
            @Override public void messages(Messages messages) {
                // TODO: process messages.
            }

            @Override public void openEditor(ResourceKey file, @Nullable Region region) {
                final @Nullable WrapsEclipseResource resource;
                try {
                    resource = resourceService.getResource(file);
                    final @Nullable IResource eclipseResource = resource.getWrappedEclipseResource();
                    if(eclipseResource == null) {
                        throw new RuntimeException("Cannot open editor for file '" + file + "', it has no corresponding Eclipse resource");
                    }
                    if(!(eclipseResource instanceof IFile)) {
                        throw new RuntimeException("Cannot open editor for Eclipse resource '" + eclipseResource + "', it is not a file");
                    }
                    final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    final IEditorPart editor = IDE.openEditor(page, (IFile) eclipseResource);
                    if(region != null && editor instanceof ITextEditor) {
                        final ITextEditor textEditor = (ITextEditor) editor;
                        textEditor.selectAndReveal(region.getStartOffset(), region.length());
                    }
                } catch(ClassCastException e) {
                    throw new RuntimeException("Cannot open editor for file '" + file + "', corresponding resource does not implement WrapsEclipseResource", e);
                } catch(PartInitException e) {
                    throw new RuntimeException("Cannot open editor for file '" + file + "', opening editor failed unexpectedly", e);
                }
            }

            @Override public void openEditor(String text, String name, @Nullable Region region) {
                try {
                    final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    final TextEditorInput editorInput = new TextEditorInput(text, name);
                    final IEditorPart editor = IDE.openEditor(page, editorInput, EditorsUI.DEFAULT_TEXT_EDITOR_ID);
                    if(region != null && editor instanceof ITextEditor) {
                        final ITextEditor textEditor = (ITextEditor) editor;
                        textEditor.selectAndReveal(region.getStartOffset(), region.length());
                    }
                } catch(PartInitException e) {
                    throw new RuntimeException("Cannot open editor (for text) with name '" + name + "', opening editor failed unexpectedly", e);
                }
            }
        });
    }
}
