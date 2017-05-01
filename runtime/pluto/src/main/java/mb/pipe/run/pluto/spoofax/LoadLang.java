package mb.pipe.run.pluto.spoofax;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.build.CommonPaths;
import org.metaborg.core.language.ComponentCreationConfig;
import org.metaborg.core.language.IComponentCreationConfigRequest;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;

import com.google.common.collect.Iterables;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.factory.BuilderFactory;
import build.pluto.dependency.Origin;
import build.pluto.output.OutputTransient;
import build.pluto.stamp.FileHashStamper;
import mb.pipe.run.core.model.IContext;
import mb.pipe.run.core.vfs.IResource;
import mb.pipe.run.pluto.util.ABuilder;
import mb.pipe.run.pluto.util.AInput;
import mb.pipe.run.pluto.util.Result;

public class LoadLang extends ABuilder<LoadLang.Input, OutputTransient<LoadLang.Output>> {
    public static class Input extends AInput {
        private static final long serialVersionUID = 1L;

        public final IResource location;


        public Input(IContext context, @Nullable Origin origin, IResource location) {
            super(context, origin);
            this.location = location;
        }
    }

    public static class Output implements Serializable {
        private static final long serialVersionUID = 1L;

        public transient final ILanguageImpl langImpl;


        public Output(ILanguageImpl langImpl) {
            this.langImpl = langImpl;
        }
    }


    public static final BuilderFactory<Input, OutputTransient<Output>, LoadLang> factory =
        factory(LoadLang.class, Input.class);

    public static
        BuildRequest<Input, OutputTransient<Output>, LoadLang, BuilderFactory<Input, OutputTransient<Output>, LoadLang>>
        request(Input input) {
        return request(input, LoadLang.class, Input.class);
    }

    public static Origin origin(Input input) {
        return origin(input, LoadLang.class, Input.class);
    }

    public static Result<Output> requireBuild(Builder<?, ?> requiree, Input input) throws IOException {
        final BuildRequest<Input, OutputTransient<Output>, LoadLang, BuilderFactory<Input, OutputTransient<Output>, LoadLang>> br =
            request(input, LoadLang.class, Input.class);
        final Origin origin = Origin.from(br);
        final Output out = requiree.requireBuild(br).val();
        return new Result<Output>(out, origin);
    }

    public static ILanguageImpl build(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, LoadLang.class, Input.class).output.val().langImpl;
    }


    public LoadLang(Input input) {
        super(input);
    }


    @Override protected String description(Input input) {
        return "Load language from " + input.location;
    }

    @Override public File persistentPath(Input input) {
        return depFile("load_lang", input.location);
    }

    @Override protected OutputTransient<Output> build(Input input) throws Throwable {
        requireOrigins();

        final FileObject langResource = input.location.fileObject();
        final IComponentCreationConfigRequest request;
        if(langResource.isFile()) {
            request = spoofax().languageComponentFactory.requestFromArchive(langResource);
            require(toFile(input.location), FileHashStamper.instance);
        } else {
            request = spoofax().languageComponentFactory.requestFromDirectory(langResource);
            // HACK: hardcode required files for language in directory.
            final CommonPaths paths = new CommonPaths(langResource);
            require(toFile(paths.mbComponentConfigFile()), FileHashStamper.instance);
            require(toFile(paths.targetMetaborgDir().resolveFile("sdf.tbl")));
            require(toFile(paths.targetMetaborgDir().resolveFile("editor.esv.af")));
        }
        final ComponentCreationConfig config = spoofax().languageComponentFactory.createConfig(request);
        final ILanguageComponent component = spoofax().languageService.add(config);
        return OutputTransient.of(new Output(Iterables.get(component.contributesTo(), 0)));
    }
}
