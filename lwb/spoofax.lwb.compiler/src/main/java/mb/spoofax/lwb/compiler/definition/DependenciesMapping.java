package mb.spoofax.lwb.compiler.definition;

import mb.cfg.Dependency;
import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.cfg.task.CfgToObject;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.StatelessSerializableFunction;

public class DependenciesMapping extends StatelessSerializableFunction<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>, Result<ListView<Dependency>, CfgRootDirectoryToObjectException>> {
    public static final DependenciesMapping instance = new DependenciesMapping();

    @Override
    public Result<ListView<Dependency>, CfgRootDirectoryToObjectException> apply(Result<CfgToObject.Output, CfgRootDirectoryToObjectException> result) {
        return result.map(o -> ListView.of(o.compileLanguageInput.compileLanguageSpecificationInput().dependencies()));
    }

    private DependenciesMapping() {}

    private Object readResolve() {return instance;}
}
