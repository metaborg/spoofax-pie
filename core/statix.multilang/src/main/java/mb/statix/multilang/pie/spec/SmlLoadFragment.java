package mb.statix.multilang.pie.spec;

import dagger.Lazy;
import mb.common.result.Result;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.HierarchicalResource;
import mb.statix.multilang.MultiLang;
import mb.statix.multilang.MultiLangScope;
import mb.statix.multilang.metadata.SpecFragmentId;
import mb.statix.multilang.metadata.SpecManager;
import mb.statix.multilang.metadata.spec.ImmutableModule;
import mb.statix.multilang.metadata.spec.ImmutableSpecFragment;
import mb.statix.multilang.metadata.spec.Module;
import mb.statix.multilang.metadata.spec.SpecConfig;
import mb.statix.multilang.metadata.spec.SpecFragment;
import mb.statix.multilang.metadata.spec.SpecLoadException;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.TermType;
import org.spoofax.terms.util.TermUtils;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Collectors;

@MultiLangScope
public class SmlLoadFragment implements TaskDef<SpecFragmentId, Result<SpecFragment, SpecLoadException>> {

    private final Lazy<SpecManager> specManager;

    @Inject public SmlLoadFragment(@MultiLang Lazy<SpecManager> specManager) {
        this.specManager = specManager;
    }

    @Override public String getId() {
        return SmlLoadFragment.class.getSimpleName();
    }

    @Override public Result<SpecFragment, SpecLoadException> exec(ExecContext context, SpecFragmentId input) throws Exception {
        return specManager.get().getSpecConfig(input).flatMap(config -> {
            try {
                return Result.ofOk(loadSpec(input, config, context));
            } catch(SpecLoadException e) {
                return Result.ofErr(e);
            } catch(IOException e) {
                return Result.ofErr(new SpecLoadException("Error loading fragment" + input, e));
            }
        });
    }

    private static SpecFragment loadSpec(SpecFragmentId id, SpecConfig config, ExecContext context) throws SpecLoadException, IOException {
        HierarchicalResource root = config.rootPackage();
        context.require(root);
        StrategoTerms strategoTerms = new StrategoTerms(config.termFactory());
        ArrayList<String> loadedModules = new ArrayList<>();
        ArrayList<String> delayedModules = new ArrayList<>();
        ArrayList<Module> fileSpecs = new ArrayList<>();
        Queue<String> modulesToLoad = new PriorityQueue<>(config.rootModules());

        while(!modulesToLoad.isEmpty()) {
            String currentModule = modulesToLoad.remove();
            // Load spec file content
            HierarchicalResource res = root.appendRelativePath(currentModule).appendExtensionToLeaf("spec.aterm");
            context.require(res);
            try {
                if(!res.exists()) {
                    delayedModules.add(currentModule);
                    continue;
                }
            } catch(IOException e) {
                throw new SpecLoadException(e);
            }

            try(BufferedReader specReader = new BufferedReader(new InputStreamReader(res.openRead()))) {
                String specString = specReader.lines().collect(Collectors.joining("\n"));
                IStrategoTerm stxFileSpec = config.termFactory().parseFromString(specString);

                // Update pointers
                fileSpecs.add(ImmutableModule.of(currentModule, strategoTerms.fromStratego(stxFileSpec)));
                loadedModules.add(currentModule);

                // Queue newly imported files
                IStrategoTerm imports = stxFileSpec.getSubterm(0);
                if(imports.getType() != TermType.LIST) {
                    throw new SpecLoadException("Invalid spec file. Imports section should be a list, but was: " + imports);
                }
                for(IStrategoTerm importDecl : imports) {
                    if(!TermUtils.isString(importDecl)) {
                        throw new SpecLoadException("Invalid file spec. Import module should be string, but was: " + importDecl);
                    }
                    String importedModule = ((IStrategoString)importDecl).stringValue();
                    if(!loadedModules.contains(importedModule) && !modulesToLoad.contains(importedModule) && !delayedModules.contains(importedModule)) {
                        modulesToLoad.add(importedModule);
                    }
                }
            } catch(IOException e) {
                throw new SpecLoadException(e);
            }
        }

        // Create builder for files
        return ImmutableSpecFragment.of(id, fileSpecs, delayedModules);
    }
}
