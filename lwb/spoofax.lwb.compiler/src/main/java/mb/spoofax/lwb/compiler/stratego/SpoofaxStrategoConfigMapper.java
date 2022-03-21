package mb.spoofax.lwb.compiler.stratego;

import mb.cfg.metalang.CfgStrategoConfig;
import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.cfg.task.CfgToObject;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.pie.api.StatelessSerializableFunction;

class SpoofaxStrategoConfigMapper extends StatelessSerializableFunction<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>, Result<Option<CfgStrategoConfig>, CfgRootDirectoryToObjectException>> {
    @Override
    public Result<Option<CfgStrategoConfig>, CfgRootDirectoryToObjectException> apply(Result<CfgToObject.Output, CfgRootDirectoryToObjectException> result) {
        return result.map(o -> Option.ofOptional(o.compileLanguageInput.compileLanguageSpecificationInput().stratego()));
    }
}
