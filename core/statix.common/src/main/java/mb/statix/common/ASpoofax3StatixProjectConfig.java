package mb.statix.common;

import mb.statix.spoofax.SolverMode;
import org.immutables.value.Value;

@Value.Immutable
public abstract class ASpoofax3StatixProjectConfig {

    @Value.Parameter public abstract SolverMode solverMode();

}
