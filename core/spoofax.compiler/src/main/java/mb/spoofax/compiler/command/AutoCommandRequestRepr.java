package mb.spoofax.compiler.command;

import mb.spoofax.compiler.util.TypeInfo;
import mb.spoofax.core.language.command.HierarchicalResourceType;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

@Value.Immutable
public interface AutoCommandRequestRepr extends Serializable {
    class Builder extends ImmutableAutoCommandRequestRepr.Builder {}

    static Builder builder() {
        return new Builder();
    }

    static AutoCommandRequestRepr of(TypeInfo commandDef, HierarchicalResourceType... resourceTypes) {
        return builder().commandDef(commandDef).addResourceTypes(resourceTypes).build();
    }

    static AutoCommandRequestRepr of(TypeInfo commandDef, Map<String, String> initialArgs) {
        return builder().commandDef(commandDef).initialArgs(initialArgs).build();
    }

    static AutoCommandRequestRepr of(TypeInfo commandDef, Map<String, String> initialArgs, HierarchicalResourceType... resourceTypes) {
        return builder().commandDef(commandDef).initialArgs(initialArgs).addResourceTypes(resourceTypes).build();
    }


    TypeInfo commandDef();

    Map<String, String> initialArgs();

    Set<HierarchicalResourceType> resourceTypes();
}
