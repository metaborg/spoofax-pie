package mb.spoofax.lwb.eclipse.dynamicloading;

import mb.spoofax.eclipse.command.RunCommandHandler;
import mb.spoofax.lwb.eclipse.SpoofaxLwbLifecycleParticipant;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class DynamicRunCommandHandler extends AbstractHandler {
    @Override public @Nullable Object execute(ExecutionEvent event) throws ExecutionException {
        final @Nullable String languageId = event.getParameter(RunCommandHandler.languageIdParameterId);
        if(languageId == null) {
            throw new ExecutionException("Cannot execute command, no argument for '" + RunCommandHandler.languageIdParameterId + "' parameter was set");
        }
        final @Nullable EclipseDynamicLanguage language = (EclipseDynamicLanguage)SpoofaxLwbLifecycleParticipant.getInstance().getDynamicLoadingComponent().getDynamicComponentManager().getLanguageForId(languageId);
        if(language == null) {
            throw new ExecutionException("Cannot execute command, no dynamic language for ID '" + languageId + "' was found");
        }
        return language.getRunCommandHandler().execute(event);
    }
}
