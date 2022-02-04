package mb.spoofax.lwb.eclipse.dynamicloading;

import mb.common.option.Option;
import mb.spoofax.core.Coordinate;
import mb.spoofax.eclipse.command.RunCommandHandler;
import mb.spoofax.lwb.eclipse.SpoofaxLwbParticipant;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class DynamicRunCommandHandler extends AbstractHandler {
    @Override public @Nullable Object execute(ExecutionEvent event) throws ExecutionException {
        final @Nullable String languageCoordinateString = event.getParameter(RunCommandHandler.languageCoordinateParameterId);
        if(languageCoordinateString == null) {
            throw new ExecutionException("Cannot execute command, no argument for '" + RunCommandHandler.languageCoordinateParameterId + "' parameter was set");
        }
        final Option<Coordinate> languageCoordinate = Coordinate.parse(languageCoordinateString);
        if(languageCoordinate.isNone()) {
            throw new ExecutionException("Cannot execute command, could not parse '" + languageCoordinateString + "' into a Coordinate");
        }
        final @Nullable EclipseDynamicLanguage language = (EclipseDynamicLanguage)SpoofaxLwbParticipant.getInstance().getDynamicLoadingComponent().getDynamicComponentManager().getLanguageForId(languageCoordinateString);
        if(language == null) {
            throw new ExecutionException("Cannot execute command, no dynamic language for ID '" + languageCoordinateString + "' was found");
        }
        return language.getRunCommandHandler().execute(event);
    }
}
