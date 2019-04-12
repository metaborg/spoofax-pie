package mb.spoofax.core.pie;

import dagger.Component;
import mb.pie.dagger.TaskDefsComponent;

@Component(modules = {SpoofaxTaskDefsModule.class})
public interface SpoofaxTaskDefsComponent extends TaskDefsComponent {
}
