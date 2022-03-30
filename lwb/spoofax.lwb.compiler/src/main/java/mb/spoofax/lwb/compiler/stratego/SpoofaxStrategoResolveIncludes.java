package mb.spoofax.lwb.compiler.stratego;

import mb.cfg.task.CfgRootDirectoryToObject;
import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.cfg.task.CfgToObject;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.StatelessSerializableFunction;
import mb.pie.task.archive.UnarchiveFromJar;
import mb.resource.hierarchical.match.path.string.PathStringMatcher;
import mb.spoofax.lwb.compiler.SpoofaxLwbCompilerComponentManagerWrapper;
import mb.spoofax.lwb.compiler.SpoofaxLwbCompilerScope;
import mb.spoofax.lwb.compiler.definition.LanguageDefinitionManager;
import mb.spoofax.lwb.compiler.definition.ResolveIncludes;

import javax.inject.Inject;
import javax.inject.Provider;

@SpoofaxLwbCompilerScope
public class SpoofaxStrategoResolveIncludes extends ResolveIncludes {
    @Inject public SpoofaxStrategoResolveIncludes(
        CfgRootDirectoryToObject cfgRootDirectoryToObject,
        LanguageDefinitionManager languageDefinitionManager,
        SpoofaxLwbCompilerComponentManagerWrapper componentManagerWrapper,
        UnarchiveFromJar unarchiveFromJar,
        Provider<SpoofaxStrategoConfigure> configureTaskDefProvider
    ) {
        super(
            cfgRootDirectoryToObject,
            languageDefinitionManager,
            componentManagerWrapper,
            unarchiveFromJar,
            PathStringMatcher.ofExtensions("str2", "str"),
            "Stratego",
            configureTaskDefProvider,
            ExportsMapper.instance,
            "Stratego"
        );
    }

    @Override public String getId() {
        return getClass().getName();
    }

    static class ExportsMapper extends StatelessSerializableFunction<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>, Result<Option<ListView<String>>, CfgRootDirectoryToObjectException>> {
        public static final ExportsMapper instance = new ExportsMapper();

        @Override
        public Result<Option<ListView<String>>, CfgRootDirectoryToObjectException> apply(Result<CfgToObject.Output, CfgRootDirectoryToObjectException> result) {
            return result.map(o -> Option.ofOptional(o.compileLanguageInput.compileLanguageSpecificationInput().stratego()).map(c -> ListView.of(c.source().getFiles().exportDirectories())));
        }

        private ExportsMapper() {}

        private Object readResolve() {return instance;}
    }
}
