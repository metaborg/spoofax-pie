package mb.statix.common;

import mb.statix.spoofax.STX_analysis_has_errors;
import mb.statix.spoofax.STX_compare_patterns;
import mb.statix.spoofax.STX_debug_scopegraph;
import mb.statix.spoofax.STX_delays_as_errors;
import mb.statix.spoofax.STX_extract_messages;
import mb.statix.spoofax.STX_get_ast_property;
import mb.statix.spoofax.STX_get_scopegraph_data;
import mb.statix.spoofax.STX_get_scopegraph_edges;
import mb.statix.spoofax.STX_is_analysis;
import mb.statix.spoofax.STX_solve_constraint;
import mb.statix.spoofax.STX_solve_multi_file;
import mb.statix.spoofax.STX_solve_multi_project;
import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;

public class StatixPrimitiveLibrary extends AbstractStrategoOperatorRegistry {
    public StatixPrimitiveLibrary() {
        add(new STX_analysis_has_errors());
        add(new STX_compare_patterns());
        add(new STX_debug_scopegraph());
        add(new STX_delays_as_errors());
        add(new STX_extract_messages());
        add(new STX_get_ast_property());
        add(new STX_get_scopegraph_data());
        add(new STX_get_scopegraph_edges());
        add(new STX_is_analysis());
        add(new STX_solve_constraint());
        add(new STX_solve_multi_file());
        add(new STX_solve_multi_project());
    }

    @Override public String getOperatorRegistryName() {
        return "Statix";
    }
}
