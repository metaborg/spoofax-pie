package mb.spoofax.runtime;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import mb.nabl2.spoofax.primitives.*;
import mb.spoofax.runtime.cfg.ConfigParser;
import mb.spoofax.runtime.esv.StylingRulesFromESV;
import mb.spoofax.runtime.nabl.*;
import mb.spoofax.runtime.stratego.primitive.ScopeGraphPrimitiveLibrary;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.IOperatorRegistry;

public class SpoofaxRuntimeModule extends AbstractModule {
    @Override protected void configure() {
        bind(StylingRulesFromESV.class).in(Singleton.class);
        bind(ConfigParser.class).in(Singleton.class);

        bindStrategoPrimitives();
    }

    protected void bindStrategoPrimitives() {
        final Multibinder<IOperatorRegistry> libraryBinder =
            Multibinder.newSetBinder(binder(), IOperatorRegistry.class);

        final Multibinder<AbstractPrimitive> spoofaxScopeGraphLibrary =
            Multibinder.newSetBinder(binder(), AbstractPrimitive.class, Names.named(ScopeGraphPrimitiveLibrary.name));
        bindPrimitive(spoofaxScopeGraphLibrary, SG_analysis_has_errors.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_debug_constraints.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_debug_name_resolution.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_debug_scope_graph.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_debug_symbolic_constraints.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_debug_unifier.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_erase_ast_indices.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_fresh.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_focus_term.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_all_decls.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_all_refs.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_all_scopes.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_ast_index.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_ast_property.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_ast_resolution.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_custom_analysis.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_decl_property.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_decl_scope.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_direct_edges_inv.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_direct_edges.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_export_edges_inv.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_export_edges.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_import_edges_inv.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_import_edges.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_reachable_decls.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_ref_resolution.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_ref_scope.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_scope_decls.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_scope_refs.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_symbolic_facts.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_symbolic_goals.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_visible_decls.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_index_ast.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_is_debug_collection_enabled.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_is_debug_custom_enabled.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_is_debug_resolution_enabled.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_set_ast_index.class);
        bindPrimitiveLibrary(libraryBinder, ScopeGraphPrimitiveLibrary.class);
    }

    protected static void bindPrimitive(Multibinder<AbstractPrimitive> binder,
        Class<? extends AbstractPrimitive> primitive) {
        binder.addBinding().to(primitive).in(Singleton.class);
    }

    protected static void bindPrimitiveLibrary(Multibinder<IOperatorRegistry> binder,
        Class<? extends IOperatorRegistry> primitiveLibrary) {
        binder.addBinding().to(primitiveLibrary).in(Singleton.class);
    }
}
