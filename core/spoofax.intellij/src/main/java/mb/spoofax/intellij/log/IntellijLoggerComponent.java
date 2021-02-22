package mb.spoofax.intellij.log;

import dagger.Component;
import mb.log.dagger.LoggerComponent;
import mb.log.dagger.LoggerScope;

@LoggerScope
@Component(
    modules = {
        IntellijLoggerModule.class
    },
    dependencies = {

    }
)
public interface IntellijLoggerComponent extends LoggerComponent {

}
