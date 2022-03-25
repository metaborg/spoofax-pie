package mb.spoofax.lwb.compiler;

import mb.esv.EsvScope;
import mb.spoofax.core.component.ComponentManager;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

@SpoofaxLwbCompilerScope
public class SpoofaxLwbCompilerComponentManagerWrapper {
    private @Nullable ComponentManager componentManager = null;


    @Inject public SpoofaxLwbCompilerComponentManagerWrapper() {}


    public ComponentManager get() {
        if(componentManager == null) {
            throw new IllegalStateException("Component manager in SpoofaxLwbCompilerComponentManagerWrapper was not set. First set the component manager");
        }
        return componentManager;
    }

    public void set(ComponentManager componentManager) {
        if(this.componentManager != null) {
            throw new IllegalStateException("Component manager in SpoofaxLwbCompilerComponentManagerWrapper was already set or used. After setting or using the component manager, it may not be changed any more to guarantee sound incrementality");
        }
        this.componentManager = componentManager;
    }
}
