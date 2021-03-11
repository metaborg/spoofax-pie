package mb.sdf3.task.spec;

import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.Sdf3Desugar;
import mb.sdf3.task.Sdf3Parse;
import mb.sdf3.task.util.Sdf3Util;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Sdf3Scope
public class Sdf3CreateSpec implements TaskDef<Supplier<Result<Sdf3SpecConfig, ?>>, Result<Sdf3Spec, ?>> {
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

    @Override
    public Result<Sdf3Spec, ?> exec(ExecContext context, Supplier<Result<Sdf3SpecConfig, ?>> configSupplier) throws IOException {
        return context.require(configSupplier).mapThrowing((config) -> {
            final Supplier<Result<IStrategoTerm, ?>> mainModuleAstSupplier = desugar.createSupplier(parse.createAstSupplier(config.mainFile));
            final ResourceWalker walker = Sdf3Util.createResourceWalker();
            final ResourceMatcher matcher = Sdf3Util.createResourceMatcher();
            final HierarchicalResource project = resourceService.getHierarchicalResource(config.rootDirectory);
            // Create dependency to project dir, such that this task gets re-executed when an SDF3 file is added/removed.
            context.require(project, ResourceStampers.modifiedDirRec(walker, matcher));
            final ArrayList<Supplier<? extends Result<IStrategoTerm, ?>>> modulesAstSuppliers = project
                .walk(Sdf3Util.createResourceWalker(), Sdf3Util.createResourceMatcher())
                .filter(file -> !file.getPath().equals(config.mainFile)) // Filter out main module, as it is supplied separately.
                .map(file -> desugar.createSupplier(parse.createAstSupplier(file.getKey())))
                .collect(Collectors.toCollection(ArrayList::new));
            return new Sdf3Spec(config.parseTableConfig, mainModuleAstSupplier, ListView.of(modulesAstSuppliers));
        });
    }
}
