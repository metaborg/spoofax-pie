package mb.spoofax.lwb.dynamicloading;

import mb.spoofax.core.component.StaticComponentManager;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import javax.inject.Inject;

@DynamicLoadingScope
public class DynamicLoadGetBaseComponentManager {
    private @MonotonicNonNull StaticComponentManager baseComponentManager = null;

    @Inject public DynamicLoadGetBaseComponentManager() {}

    public boolean isSet() {
        return baseComponentManager != null;
    }

    public StaticComponentManager get() {
        if(!isSet()) {
            throw new IllegalStateException("Attempted to get base component manager before it was set. First set the base component manager once via DynamicLoadGetBaseComponentManager");
        }
        return baseComponentManager;
    }

    public void set(StaticComponentManager baseComponentManager) {
        if(isSet()) {
            throw new IllegalStateException("Attempted to set base component manager after it was already set via DynamicLoadGetBaseComponentManager. After setting the base component manager, it may not be changed any more to guarantee sound incrementality");
        }
        this.baseComponentManager = baseComponentManager;
    }
}
