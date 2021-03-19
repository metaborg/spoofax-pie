package mb.spoofax.lwb.compiler.statix;

import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.cfg.task.CfgToObject;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.StatixConfig;

public class StatixConfigFunction implements Function<ResourcePath, Result<Option<StatixConfig>, ?>> {
    private final Function<ResourcePath, Result<CfgToObject.Output, CfgRootDirectoryToObjectException>> cfgRootDirectoryToObject;

    public StatixConfigFunction(Function<ResourcePath, Result<CfgToObject.Output, CfgRootDirectoryToObjectException>> cfgRootDirectoryToObject) {
        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;
    }

    @Override public Result<Option<StatixConfig>, ?> apply(ExecContext context, ResourcePath rootDirectory) {
        final Result<CfgToObject.Output, CfgRootDirectoryToObjectException> result = context.require(cfgRootDirectoryToObject, rootDirectory);
        return result.map(o -> Option.ofOptional(o.compileLanguageToJavaClassPathInput.compileLanguageInput().statix().map(statix ->
            new StatixConfig(statix.sourceDirectory())
        )));
    }
}
