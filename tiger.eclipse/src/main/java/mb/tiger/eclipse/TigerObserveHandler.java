package mb.tiger.eclipse;

import mb.pie.api.ExecException;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.pie.PieRunner;
import mb.spoofax.eclipse.util.AbstractHandlerUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;

import java.util.ArrayList;

public class TigerObserveHandler extends AbstractHandler {
    private final PieRunner pieRunner;


    public TigerObserveHandler() {
        this.pieRunner = SpoofaxPlugin.getComponent().getPieRunner();
    }


    @Override public @Nullable Object execute(@NonNull ExecutionEvent event) throws ExecutionException {
        final ArrayList<IFile> files = AbstractHandlerUtil.toFiles(event);
        if(files.isEmpty()) return null;
        try {
            pieRunner.observeCheckTasks(TigerPlugin.getComponent(), files, null);
        } catch(ExecException e) {
            throw new ExecutionException("Observing files '" + files + "' failed unexpectedly", e);
        } catch(InterruptedException e) {
            // Ignore
        }
        return null;
    }
}
