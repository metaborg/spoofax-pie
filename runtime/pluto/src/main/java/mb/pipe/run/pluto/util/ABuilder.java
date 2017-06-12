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
import mb.pipe.run.core.PipeRunEx;
import mb.pipe.run.core.StaticPipeFacade;
import mb.pipe.run.core.path.PPath;
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


    protected static File toFile(PPath resource) {
        final File file = pipe().pathSrv.localPath(resource);
        if(file == null) {
            throw new PipeRunEx("Cannot convert " + resource + " to a local file, it is not on the local file system");
        }
        return file;
    }

    protected static File toFileReplicate(PPath resource) {
        return pipe().pathSrv.localFile(resource);
    }

    protected void require(PPath resource) {
        require(toFile(resource));
    }

    protected void provide(PPath resource) {
        provide(toFile(resource));
    }


    protected static File toFile(FileObject fileObject) {
        final File file = spoofax().resourceService.localPath(fileObject);
        if(file == null) {
            throw new PipeRunEx(
                "Cannot convert " + fileObject + " to a local file, it is not on the local file system");
        }
        return file;
    }


    protected File persistentDir() {
        final PPath persistentDir = getInput().context.persistentDir();
        final File localPersistentDir = pipe().pathSrv.localPath(persistentDir);
        if(localPersistentDir == null) {
            throw new PipeRunEx(
                "Could not get persistent directory at " + persistentDir + ", it is not on the local filesystem");
        }
        return localPersistentDir;
    }

    protected File depFile(String relative) {
        return new File(persistentDir(), relative + ".dep");
    }

    protected File depFile(String name, Object... objs) {
        final String str = Arrays.toString(objs);
        final String hash = hash(str);
        return new File(persistentDir(), name + "-" + hash + ".dep");
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
