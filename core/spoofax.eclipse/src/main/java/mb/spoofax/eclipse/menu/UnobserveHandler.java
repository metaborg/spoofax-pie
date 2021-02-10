package mb.spoofax.eclipse.menu;

import mb.common.util.SetView;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.pie.PieRunner;
import mb.spoofax.eclipse.util.AbstractHandlerUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;

import java.util.ArrayList;

public class UnobserveHandler extends AbstractHandler {
    private final PieRunner pieRunner;

    private final EclipseLanguageComponent languageComponent;


    public UnobserveHandler(EclipseLanguageComponent languageComponent) {
        this.pieRunner = SpoofaxPlugin.getPlatformComponent().getPieRunner();

        this.languageComponent = languageComponent;
    }


    @Override public @Nullable Object execute(@NonNull ExecutionEvent event) {
        final ArrayList<IFile> files = AbstractHandlerUtil.toFiles(event);
        final SetView<String> extensions = languageComponent.getLanguageInstance().getFileExtensions();
        files.removeIf((f) -> !extensions.contains(f.getFileExtension()));
        if(files.isEmpty()) return null;
        // TODO: reimplement single/multi-file inspections.
//        pieRunner.unobserveCheckTasks(languageComponent, files, null);
        return null;
    }
}
