package mb.spoofax.lwb.compiler.sdf3;

import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.cfg.task.CfgToObject;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import org.metaborg.sdf2table.parsetable.ParseTableConfiguration;

public class Sdf3ConfigFunction implements Function<ResourcePath, Result<Option<Sdf3SpecConfig>, ?>> {
    private final Function<ResourcePath, Result<CfgToObject.Output, CfgRootDirectoryToObjectException>> cfgRootDirectoryToObject;

    public Sdf3ConfigFunction(Function<ResourcePath, Result<CfgToObject.Output, CfgRootDirectoryToObjectException>> cfgRootDirectoryToObject) {
        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;
    }

    @Override public Result<Option<Sdf3SpecConfig>, ?> apply(ExecContext context, ResourcePath rootDirectory) {
        final Result<CfgToObject.Output, CfgRootDirectoryToObjectException> result = context.require(cfgRootDirectoryToObject, rootDirectory);
        return result.map(o -> Option.ofOptional(o.compileLanguageToJavaClassPathInput.compileLanguageInput().sdf3().map(sdf3 ->
            new Sdf3SpecConfig(sdf3.sourceDirectory(), sdf3.mainFile(), new ParseTableConfiguration(
                sdf3.createDynamicParseTable(),
                sdf3.createDataDependentParseTable(),
                sdf3.solveDeepConflictsInParseTable(),
                sdf3.checkOverlapInParseTable(),
                sdf3.checkPrioritiesInParseTable(),
                sdf3.createLayoutSensitiveParseTable()
            ))
        )));
    }
}
