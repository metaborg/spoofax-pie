package mb.spoofax.lwb.compiler;

import mb.spoofax.core.component.ComponentManager;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

@SpoofaxLwbCompilerScope
public class SpoofaxLwbCompilerComponentManagerWrapper {
    private @Nullable ComponentManager componentManager = null;
    private boolean used = false;


    @Inject public SpoofaxLwbCompilerComponentManagerWrapper() {}


    public ComponentManager get() {
        used = true;
        if(componentManager == null) {
            throw new IllegalStateException("Component manager in SpoofaxLwbCompilerComponentManagerWrapper was not set. First set the component manager");
        }
        return componentManager;
    }

    public void set(ComponentManager componentManager) {
        if(used && this.componentManager != null) {
            throw new IllegalStateException("Component manager in SpoofaxLwbCompilerComponentManagerWrapper was already used. After using the component manager, it may not be changed any more to guarantee sound incrementality");
        }
        this.componentManager = componentManager;
    }
}
