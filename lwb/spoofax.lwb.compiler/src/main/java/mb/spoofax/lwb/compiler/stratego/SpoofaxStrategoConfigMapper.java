package mb.spoofax.lwb.compiler.stratego;

import mb.cfg.metalang.CfgStrategoConfig;
import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.cfg.task.CfgToObject;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.pie.api.StatelessSerializableFunction;

class SpoofaxStrategoConfigMapper extends StatelessSerializableFunction<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>, Result<Option<CfgStrategoConfig>, CfgRootDirectoryToObjectException>> {
    public static final SpoofaxStrategoConfigMapper instance = new SpoofaxStrategoConfigMapper();

    @Override
    public Result<Option<CfgStrategoConfig>, CfgRootDirectoryToObjectException> apply(Result<CfgToObject.Output, CfgRootDirectoryToObjectException> result) {
        return result.map(o -> Option.ofOptional(o.compileLanguageInput.compileLanguageSpecificationInput().stratego()));
    }

    private SpoofaxStrategoConfigMapper() {}

    private Object readResolve() {return instance;}
}
