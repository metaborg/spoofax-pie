package mb.str.spoofax;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.common.result.Result;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.log.slf4j.SLF4JLoggerFactory;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.ResourceStringSupplier;
import mb.pie.api.Supplier;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import mb.resource.fs.FSResource;
import mb.resource.text.TextResource;
import mb.resource.text.TextResourceRegistry;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformPieModule;
import mb.str.spoofax.task.StrategoParse;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;

public class TestBase {
    public final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
    public final FSResource rootDirectory = new FSResource(fileSystem.getPath("/"));

    public final PlatformTestComponent platformComponent = DaggerPlatformTestComponent
        .builder()
        .loggerFactoryModule(new LoggerFactoryModule(new SLF4JLoggerFactory()))
        .platformPieModule(new PlatformPieModule(PieBuilderImpl::new))
        .build();
    public final LoggerFactory loggerFactory = platformComponent.getLoggerFactory();
    public final Logger log = loggerFactory.create(TestBase.class);
    public final TextResourceRegistry textResourceRegistry = platformComponent.getTextResourceRegistry();

    public final StrategoComponent languageComponent = DaggerStrategoComponent
        .builder()
        .platformComponent(platformComponent)
        .build();
    public final StrategoParse parse = languageComponent.getStrategoParse();
    public final Pie pie = languageComponent.getPie();


    public FSResource createTextFile(String text, String relativePath) throws IOException {
        final FSResource resource = rootDirectory.appendRelativePath(relativePath);
        resource.writeString(text, StandardCharsets.UTF_8);
        return resource;
    }

    public TextResource createTextResource(String text, String id) {
        return textResourceRegistry.createResource(text, id);
    }


    public ResourceStringSupplier resourceStringSupplier(ResourceKey resourceKey) {
        return new ResourceStringSupplier(resourceKey);
    }

    public ResourceStringSupplier resourceStringSupplier(Resource resource) {
        return resourceStringSupplier(resource.getKey());
    }


    public Supplier<? extends Result<IStrategoTerm, ?>> parsedAstSupplier(ResourceKey resourceKey) {
        return parse.createAstSupplier(resourceKey);
    }

    public Supplier<? extends Result<IStrategoTerm, ?>> parsedAstSupplier(Resource resource) {
        return parsedAstSupplier(resource.getKey());
    }


    public MixedSession newSession() {
        return pie.newSession();
    }
}
