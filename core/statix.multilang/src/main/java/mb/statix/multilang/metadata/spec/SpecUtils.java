package mb.statix.multilang.metadata.spec;

import mb.common.result.Result;
import mb.nabl2.terms.matching.TermMatch.IMatcher;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.resource.hierarchical.HierarchicalResource;
import mb.statix.spec.Rule;
import mb.statix.spec.RuleSet;
import mb.statix.spec.Spec;
import mb.statix.spoofax.StatixTerms;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.interpreter.terms.TermType;
import org.spoofax.terms.util.TermUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import static mb.nabl2.terms.matching.TermMatch.M;

public class SpecUtils {

    public static SpecFragment loadSpec(HierarchicalResource root, String initialModulePath, ITermFactory termFactory) throws SpecLoadException {
        return loadSpec(root, Collections.singleton(initialModulePath), termFactory);
    }

    public static SpecFragment loadSpec(HierarchicalResource root, Collection<String> initialModulePaths, ITermFactory termFactory) throws SpecLoadException {
        StrategoTerms strategoTerms = new StrategoTerms(termFactory);
        ArrayList<String> loadedModules = new ArrayList<>();
        ArrayList<String> delayedModules = new ArrayList<>();
        ArrayList<Module> fileSpecs = new ArrayList<>();
        Queue<String> modulesToLoad = new PriorityQueue<>(initialModulePaths);

        while(!modulesToLoad.isEmpty()) {
            String currentModule = modulesToLoad.remove();
            // Load spec file content
            HierarchicalResource res = root.appendRelativePath(currentModule).appendExtensionToLeaf("spec.aterm");
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
                IStrategoTerm stxFileSpec = termFactory.parseFromString(specString);

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
        return ImmutableSpecFragment.of(fileSpecs, delayedModules);
    }

    public static Result<Spec, SpecLoadException> mergeSpecs(Result<Spec, SpecLoadException> accResult, Result<Spec, SpecLoadException> newSpecResult) {
        return accResult.mapOrElse(acc -> newSpecResult.mapOrElse(newSpec -> Result.ofOk(mergeSpecs(acc, newSpec)), Result::ofErr),
            accErr -> newSpecResult.mapOrElse(
                res -> Result.ofErr(accErr),
                err -> {
                    accErr.addSuppressed(err);
                    return Result.ofErr(accErr);
                }
            ));
    }

    public static Spec mergeSpecs(Spec acc, Spec newSpec) {
        Set<Rule> rules = new HashSet<>(acc.rules().getAllRules());
        rules.addAll(newSpec.rules().getAllRules());

        return Spec.builder()
            .from(acc)
            .rules(RuleSet.of(rules))
            .addAllEdgeLabels(newSpec.edgeLabels())
            .addAllDataLabels(newSpec.dataLabels())
            .putAllScopeExtensions(newSpec.scopeExtensions())
            .build();
    }

    public static IMatcher<Spec> fileSpec() {
        return M.appl6("FileSpec", M.list(), M.req(StatixTerms.labels()), M.req(StatixTerms.labels()), M.term(), StatixTerms.rules(), M.req(StatixTerms.scopeExtensions()),
            (t, l, edgeLabels, dataLabels, noRelationLabel, rules, ext) -> Spec.of(rules, edgeLabels, dataLabels, ext));
    }
}
