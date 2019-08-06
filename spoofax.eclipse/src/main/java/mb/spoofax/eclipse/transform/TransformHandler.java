package mb.spoofax.eclipse.transform;

import mb.common.util.MapView;
import mb.common.util.SerializationUtil;
import mb.pie.api.ExecException;
import mb.pie.api.PieSession;
import mb.spoofax.core.language.transform.TransformDef;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.SpoofaxEclipseComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.pie.PieRunner;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import java.util.HashMap;

public class TransformHandler extends AbstractHandler {
    public final static String dataParameterId = "data";

    private final EclipseLanguageComponent languageComponent;

    private final PieRunner pieRunner;

    private final MapView<String, TransformDef> transformDefsPerId;


    public TransformHandler(EclipseLanguageComponent languageComponent) {
        this.languageComponent = languageComponent;

        final SpoofaxEclipseComponent component = SpoofaxPlugin.getComponent();
        this.pieRunner = component.getPieRunner();

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
        final TransformData data = SerializationUtil.deserialize(dataStr, TransformHandler.class.getClassLoader());
        final @Nullable TransformDef def = transformDefsPerId.get(data.transformId);
        if(def == null) {
            throw new ExecutionException("Cannot execute transform with ID '" + data.transformId + "', transform with that ID was not found in language '" + languageComponent.getLanguageInstance().getDisplayName() + "'");
        }
        try {
            try(final PieSession session = languageComponent.newPieSession()) {
                pieRunner.requireTransform(languageComponent, def, data.executionType, data.contexts, session, null);
            }
        } catch(ExecException e) {
            throw new ExecutionException("Cannot execute transform '" + def + "', execution failed unexpectedly", e);
        } catch(InterruptedException e) {
            // Ignore
        }
        return null;
    }
}
