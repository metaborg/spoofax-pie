package mb.nabl2.common;

import mb.nabl2.spoofax.primitives.SG_analysis_has_errors;
import mb.nabl2.spoofax.primitives.SG_debug_constraints;
import mb.nabl2.spoofax.primitives.SG_debug_name_resolution;
import mb.nabl2.spoofax.primitives.SG_debug_scope_graph;
import mb.nabl2.spoofax.primitives.SG_debug_symbolic_constraints;
import mb.nabl2.spoofax.primitives.SG_debug_unifier;
import mb.nabl2.spoofax.primitives.SG_focus_term;
import mb.nabl2.spoofax.primitives.SG_get_all_decls;
import mb.nabl2.spoofax.primitives.SG_get_all_refs;
import mb.nabl2.spoofax.primitives.SG_get_all_scopes;
import mb.nabl2.spoofax.primitives.SG_get_ast_decls;
import mb.nabl2.spoofax.primitives.SG_get_ast_property;
import mb.nabl2.spoofax.primitives.SG_get_ast_refs;
import mb.nabl2.spoofax.primitives.SG_get_ast_resolution;
import mb.nabl2.spoofax.primitives.SG_get_custom_analysis;
import mb.nabl2.spoofax.primitives.SG_get_decl_property;
import mb.nabl2.spoofax.primitives.SG_get_decl_resolution;
import mb.nabl2.spoofax.primitives.SG_get_decl_scope;
import mb.nabl2.spoofax.primitives.SG_get_direct_edges;
import mb.nabl2.spoofax.primitives.SG_get_direct_edges_inv;
import mb.nabl2.spoofax.primitives.SG_get_export_edges;
import mb.nabl2.spoofax.primitives.SG_get_export_edges_inv;
import mb.nabl2.spoofax.primitives.SG_get_import_edges;
import mb.nabl2.spoofax.primitives.SG_get_import_edges_inv;
import mb.nabl2.spoofax.primitives.SG_get_reachable_decls;
import mb.nabl2.spoofax.primitives.SG_get_ref_resolution;
import mb.nabl2.spoofax.primitives.SG_get_ref_scope;
import mb.nabl2.spoofax.primitives.SG_get_scope_decls;
import mb.nabl2.spoofax.primitives.SG_get_scope_refs;
import mb.nabl2.spoofax.primitives.SG_get_symbolic_facts;
import mb.nabl2.spoofax.primitives.SG_get_symbolic_goals;
import mb.nabl2.spoofax.primitives.SG_get_visible_decls;
import mb.nabl2.spoofax.primitives.SG_set_custom_analysis;
import mb.nabl2.spoofax.primitives.SG_solve_multi_final_constraint;
import mb.nabl2.spoofax.primitives.SG_solve_multi_initial_constraint;
import mb.nabl2.spoofax.primitives.SG_solve_multi_unit_constraint;
import mb.nabl2.spoofax.primitives.SG_solve_single_constraint;
import mb.nabl2.terms.stratego.primitives.SG_erase_ast_indices;
import mb.nabl2.terms.stratego.primitives.SG_get_ast_index;
import mb.nabl2.terms.stratego.primitives.SG_get_ast_resource;
import mb.nabl2.terms.stratego.primitives.SG_index_ast;
import mb.nabl2.terms.stratego.primitives.SG_set_ast_index;
import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;

public class NaBL2PrimitiveLibrary extends AbstractStrategoOperatorRegistry {
    public NaBL2PrimitiveLibrary() {
        // nabl2.terms
        add(new SG_erase_ast_indices());
        add(new SG_get_ast_index());
        add(new SG_get_ast_resource());
        add(new SG_index_ast());
        add(new SG_set_ast_index());
        // nabl2.solver
        add(new SG_analysis_has_errors());
        add(new SG_debug_constraints());
        add(new SG_debug_name_resolution());
        add(new SG_debug_scope_graph());
        add(new SG_debug_symbolic_constraints());
        add(new SG_debug_unifier());
        add(new SG_focus_term());
        add(new SG_get_all_decls());
        add(new SG_get_all_refs());
        add(new SG_get_all_scopes());
        add(new SG_get_ast_decls());
        add(new SG_get_ast_property());
        add(new SG_get_ast_refs());
        add(new SG_get_ast_resolution());
        add(new SG_get_custom_analysis());
        add(new SG_get_decl_property());
        add(new SG_get_decl_resolution());
        add(new SG_get_decl_scope());
        add(new SG_get_direct_edges_inv());
        add(new SG_get_direct_edges());
        add(new SG_get_export_edges_inv());
        add(new SG_get_export_edges());
        add(new SG_get_import_edges_inv());
        add(new SG_get_import_edges());
        add(new SG_get_reachable_decls());
        add(new SG_get_ref_resolution());
        add(new SG_get_ref_scope());
        add(new SG_get_scope_decls());
        add(new SG_get_scope_refs());
        add(new SG_get_symbolic_facts());
        add(new SG_get_symbolic_goals());
        add(new SG_get_visible_decls());
        add(new SG_is_debug_collection_enabled()); // Mocked
        add(new SG_is_debug_custom_enabled()); // Mocked
        add(new SG_is_debug_resolution_enabled()); // Mocked
        add(new SG_set_custom_analysis());
        add(new SG_solve_single_constraint());
        add(new SG_solve_multi_initial_constraint());
        add(new SG_solve_multi_unit_constraint());
        add(new SG_solve_multi_final_constraint());
    }

    @Override public String getOperatorRegistryName() {
        return "NaBL2";
    }
}
