package mb.statix.common;

import mb.statix.spoofax.STX_analysis_has_errors;
import mb.statix.spoofax.STX_compare_patterns;
import mb.statix.spoofax.STX_debug_scopegraph;
import mb.statix.spoofax.STX_delays_as_errors;
import mb.statix.spoofax.STX_diff_scopegraphs;
import mb.statix.spoofax.STX_extract_messages;
import mb.statix.spoofax.STX_get_all_properties;
import mb.statix.spoofax.STX_get_ast_properties;
import mb.statix.spoofax.STX_get_ast_property;
import mb.statix.spoofax.STX_get_ast_property_from_index;
import mb.statix.spoofax.STX_get_scopegraph;
import mb.statix.spoofax.STX_get_scopegraph_data;
import mb.statix.spoofax.STX_get_scopegraph_edges;
import mb.statix.spoofax.STX_incremental_diagnostics;
import mb.statix.spoofax.STX_is_analysis;
import mb.statix.spoofax.STX_labelord_lt;
import mb.statix.spoofax.STX_labelre_to_states;
import mb.statix.spoofax.STX_ords_to_relation;
import mb.statix.spoofax.STX_solve_constraint;
import mb.statix.spoofax.STX_solve_constraint_concurrent;
import mb.statix.spoofax.STX_solve_multi;
import mb.statix.spoofax.STX_solve_multi_file;
import mb.statix.spoofax.STX_solve_multi_project;
import mb.statix.spoofax.STX_test_log_level;
import mb.stratego.common.primitive.FailingPrimitive;
import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;

public class StatixPrimitiveLibrary extends AbstractStrategoOperatorRegistry {
    public StatixPrimitiveLibrary() {
        add(new STX_analysis_has_errors());
        add(new STX_compare_patterns());
        add(new STX_debug_scopegraph());
        add(new STX_delays_as_errors());
        add(new STX_diff_scopegraphs());
        add(new STX_extract_messages());
        add(new STX_get_all_properties());
        add(new STX_get_ast_properties());
        add(new STX_get_ast_property());
        add(new STX_get_ast_property_from_index());
        add(new STX_get_scopegraph());
        add(new STX_get_scopegraph_data());
        add(new STX_get_scopegraph_edges());
        add(new STX_is_analysis());
        add(new STX_solve_constraint());
        add(new STX_solve_constraint_concurrent());
        add(new STX_solve_multi());
        add(new STX_solve_multi_file());
        add(new STX_solve_multi_project());
        add(new STX_test_log_level());
        add(new STX_incremental_diagnostics());

        add(new STX_labelre_to_states(""));
        add(new STX_ords_to_relation());
        add(new STX_labelord_lt());

        add(new StatixProjectConfigPrimitive());
        add(new StatixConcurrentEnabledPrimitive());
        add(new StatixSolverModePrimitive());
    }

    @Override public String getOperatorRegistryName() {
        return "Statix";
    }
}
