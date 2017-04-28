package mb.pipe.run.pluto.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.meta.core.SpoofaxMeta;

import com.google.common.hash.Hashing;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.factory.BuilderFactory;
import build.pluto.builder.factory.BuilderFactoryFactory;
import build.pluto.dependency.Origin;
import build.pluto.output.Output;
import build.pluto.output.OutputTransient;
import mb.pipe.run.core.PipeFacade;
import mb.pipe.run.core.StaticPipeFacade;
import mb.pipe.run.spoofax.util.StaticSpoofax;

public abstract class ABuilder<In extends AInput, Out extends Output> extends Builder<In, Out> {
    protected static <In extends AInput, Out extends Output, T extends Builder<In, Out>> BuilderFactory<In, Out, T>
        factory(Class<T> t, Class<In> in) {
        return BuilderFactoryFactory.of(t, in);
    }

    protected static <In extends AInput, Out extends Output, T extends Builder<In, Out>>
        BuildRequest<In, Out, T, BuilderFactory<In, Out, T>> request(In input, Class<T> t, Class<In> in) {
        return new BuildRequest<>(ABuilder.<In, Out, T>factory(t, in), input);
    }

    protected static <In extends AInput, Out extends Output, T extends Builder<In, Out>> Origin origin(In input,
        Class<T> t, Class<In> in) {
        return Origin.from(request(input, t, in));
    }

    protected static <In extends AInput, Out extends Output, T extends Builder<In, Out>> Result<Out>
        requireBuild(Builder<?, ?> requiree, In input, Class<T> t, Class<In> in) throws IOException {
        final BuildRequest<In, Out, T, BuilderFactory<In, Out, T>> br = request(input, t, in);
        final Origin origin = Origin.from(br);
        final Out out = requiree.requireBuild(br);
        return new Result<Out>(out, origin);
    }

    protected static <In extends AInput, Out extends Output, T extends Builder<In, OutputTransient<Out>>> Result<Out>
        requireBuildTransient(Builder<?, ?> requiree, In input, Class<T> t, Class<In> in) throws IOException {
        final BuildRequest<In, OutputTransient<Out>, T, BuilderFactory<In, OutputTransient<Out>, T>> br =
            request(input, t, in);
        final Origin origin = Origin.from(br);
        final Out out = requiree.requireBuild(br).val();
        return new Result<Out>(out, origin);
    }

    public ABuilder(In input) {
        super(input);
    }

    protected static Spoofax spoofax() {
        return StaticSpoofax.spoofax();
    }

    protected static SpoofaxMeta spoofaxMeta() {
        return StaticSpoofax.spoofaxMeta();
    }

    protected static PipeFacade pipe() {
        return StaticPipeFacade.facade();
    }
    
    protected static File toFile(FileObject fileObject) {
        return spoofax().resourceService.localPath(fileObject);
    }

    protected static File toFileReplicate(FileObject fileObject) {
        return spoofax().resourceService.localFile(fileObject);
    }

    protected void require(FileObject file) {
        require(toFile(file));
    }

    protected void provide(FileObject file) {
        provide(toFile(file));
    }

    protected File depFile(String relative) {
        return new File(getInput().depDir, relative + ".dep");
    }

    protected File depFile(String name, Object... objs) {
        final String str = Arrays.toString(objs);
        final String hash = hash(str);
        return new File(getInput().depDir, name + "-" + hash + ".dep");
    }

    protected void requireOrigins() throws IOException {
        requireBuild(getInput().origin);
    }

    protected static String hash(Object... objs) {
        final String str = Arrays.toString(objs);
        return hash(str);
    }

    protected static String hash(String str) {
        return Hashing.sha256().hashString(str, StandardCharsets.UTF_8).toString();
    }

    /**
     * @param obj
     *            Dummy object.
     */
    protected static void eat(Object obj) {
        // Do nothing, used to wrap dummy expressions.
    }
}
