package mb.statix.common;

import mb.statix.spoofax.STX_compare_patterns;
import mb.statix.spoofax.STX_delays_as_errors;
import mb.statix.spoofax.STX_extract_messages;
import mb.statix.spoofax.STX_solve_constraint;
import mb.statix.spoofax.STX_solve_multi_file;
import mb.statix.spoofax.STX_solve_multi_project;
import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;

public class StatixPrimitiveLibrary extends AbstractStrategoOperatorRegistry {
    public StatixPrimitiveLibrary(String groupId, String id, String version, String locationUri) {
        add(new STX_compare_patterns());
        add(new STX_delays_as_errors());
        add(new STX_extract_messages());
        add(new STX_solve_constraint());
        add(new STX_solve_multi_file());
        add(new STX_solve_multi_project());
        add(new LanguageComponentPrimitive(groupId, id, version, locationUri));
    }

    @Override public String getOperatorRegistryName() {
        return "Statix";
    }
}
