package mb.spoofax.eclipse.log;

import dagger.Component;
import mb.log.dagger.LoggerComponent;
import mb.log.dagger.LoggerScope;

@LoggerScope
@Component(
    modules = {
        EclipseLoggerModule.class
    },
    dependencies = {

    }
)
public interface EclipseLoggerComponent extends LoggerComponent {

}
