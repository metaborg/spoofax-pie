package mb.spoofax.lwb.compiler.stratego;

import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.cfg.task.CfgToObject;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.pie.api.StatelessSerializableFunction;

class SpoofaxStrategoConfigMapper extends StatelessSerializableFunction<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>, Result<Option<SpoofaxStrategoConfig>, CfgRootDirectoryToObjectException>> {
    @Override
    public Result<Option<SpoofaxStrategoConfig>, CfgRootDirectoryToObjectException> apply(Result<CfgToObject.Output, CfgRootDirectoryToObjectException> result) {
        return result.map(o -> Option.ofOptional(o.compileLanguageInput.compileLanguageSpecificationInput().stratego()
            .map(c -> new SpoofaxStrategoConfig(
                c,
                o.compileLanguageInput.compileLanguageSpecificationInput().dependencies()
            ))));
    }
}
