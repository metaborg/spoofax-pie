package mb.sdf3.spoofax.task;

import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.sdf3.spoofax.task.util.Sdf3Util;
import mb.spoofax.core.language.LanguageScope;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@LanguageScope
public class Sdf3CreateSpec implements TaskDef<Sdf3CreateSpec.Input, Sdf3Spec> {
    public static class Input implements Serializable {
        public final ResourceKey mainFile;
        public final Collection<ResourcePath> includePaths;
        public final Collection<ResourceKey> includeFiles;

        public Input(ResourceKey mainFile, Collection<ResourcePath> includePaths, Collection<ResourceKey> includeFiles) {
            this.mainFile = mainFile;
            this.includePaths = includePaths;
            this.includeFiles = includeFiles;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input other = (Input)o;
            return this.mainFile.equals(other.mainFile)
                && this.includePaths.equals(other.includePaths)
                && this.includeFiles.equals(other.includeFiles);
        }

        @Override public int hashCode() {
            return Objects.hash(mainFile, includePaths, includeFiles);
        }

        @Override public String toString() {
            return "Input{mainFile=" + mainFile + ", includePaths=" + includePaths + ", includeFiles=" + includeFiles + " }";
        }
    }

    private final ResourceService resourceService;
    private final Sdf3Parse parse;
    private final Sdf3Desugar desugar;

    @Inject public Sdf3CreateSpec(ResourceService resourceService, Sdf3Parse parse, Sdf3Desugar desugar) {
        this.resourceService = resourceService;
        this.parse = parse;
        this.desugar = desugar;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Sdf3Spec exec(ExecContext context, Input input) throws IOException {
        final Supplier<Result<IStrategoTerm, ?>> mainModuleAstSupplier = desugar.createSupplier(parse.createAstSupplier(input.mainFile));

        // Gather the AST suppliers for the included SDF files and the SDF files in the included paths.
        final List<Supplier<? extends Result<IStrategoTerm, ?>>> modulesAstSuppliers =
                gatherResources(input.includePaths, input.includeFiles, context, Sdf3Util.createResourceWalker(), Sdf3Util.createResourceMatcher())
                .filter(key -> !key.equals(input.mainFile)) // Filter out the main module, as it is supplied separately.
                .map(key -> desugar.createSupplier(parse.createAstSupplier(key)))
                .collect(Collectors.toList());

        return new Sdf3Spec(mainModuleAstSupplier, ListView.of(modulesAstSuppliers));
    }

    /**
     * Gathers all relevant resource keys from the specified paths and files.
     *
     * This also adds a dependency on the paths, but not on the individual files.
     *
     * @param paths the paths whose files to include
     * @param files the files to include
     * @param context the context in which the resources are gathered
     * @param walker the resource walker to use
     * @param matcher the resource matcher to use
     * @return a stream of unique resources
     */
    // TODO: This should be a library function
    private Stream<ResourceKey> gatherResources(Collection<ResourcePath> paths, Collection<ResourceKey> files, ExecContext context, ResourceWalker walker, ResourceMatcher matcher) throws IOException {
        try {
            // Gather the included SDF files and the SDF files in the included paths.
            return Stream.concat(
                paths.stream().<ResourceKey>flatMap(path -> {
                    try {
                        // Create a dependency on the directory, such that this task gets re-executed when an SDF3 file is added/removed.
                        context.require(path, ResourceStampers.modifiedDirRec(walker, matcher));

                        final HierarchicalResource resource = resourceService.getHierarchicalResource(path);

                        // Find all the resources.
                        return resource.walk(walker, matcher).map(r -> r.getKey());
                    } catch(IOException e) {
                        throw new RuntimeException(e);
                    }
                }),
                files.stream()
                // TODO: Can we apply the matcher to the included files?
            ).distinct(); // Ensure we have no duplicate resources.
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException) {
                // Extract the inner IOException.
                throw (IOException)e.getCause();
            } else {
                // Rethrow.
                throw e;
            }
        }
    }
}
