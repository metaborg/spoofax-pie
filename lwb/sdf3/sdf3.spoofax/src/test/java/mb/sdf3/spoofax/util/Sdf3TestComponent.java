package mb.sdf3.spoofax.util;

import dagger.Component;
import mb.sdf3.spoofax.Sdf3Component;
import mb.sdf3.spoofax.Sdf3Module;
import mb.sdf3.spoofax.Sdf3Scope;
import mb.spoofax.core.platform.PlatformComponent;
import mb.stratego.common.StrategoRuntimeBuilder;

@Sdf3Scope @Component(modules = {Sdf3Module.class}, dependencies = PlatformComponent.class)
public interface Sdf3TestComponent extends Sdf3Component {
    StrategoRuntimeBuilder getStrategoRuntimeBuilder();
}
