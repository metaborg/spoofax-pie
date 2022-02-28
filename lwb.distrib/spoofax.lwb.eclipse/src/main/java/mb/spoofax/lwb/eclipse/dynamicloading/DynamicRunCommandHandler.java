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
        final @Nullable String componentCoordinateString = event.getParameter(RunCommandHandler.languageCoordinateParameterId);
        if(componentCoordinateString == null) {
            throw new ExecutionException("Cannot execute command, no argument for '" + RunCommandHandler.languageCoordinateParameterId + "' parameter was set");
        }
        final Option<Coordinate> componentCoordinate = Coordinate.parse(componentCoordinateString);
        if(componentCoordinate.isNone()) {
            throw new ExecutionException("Cannot execute command, could not parse '" + componentCoordinateString + "' into a Coordinate");
        }
        final @Nullable EclipseDynamicComponent component = (EclipseDynamicComponent)SpoofaxLwbParticipant.getInstance().getDynamicLoadingComponent().getDynamicComponentManager().getDynamicComponent(componentCoordinate.unwrap()).get();
        if(component == null) {
            throw new ExecutionException("Cannot execute command, no dynamic component for ID '" + componentCoordinateString + "' was found");
        }
        return component.getRunCommandHandler().execute(event);
    }
}
