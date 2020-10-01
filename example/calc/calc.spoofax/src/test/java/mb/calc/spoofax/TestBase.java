package mb.calc.spoofax;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.log.slf4j.SLF4JLoggerFactory;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.ResourceStringSupplier;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import mb.resource.fs.FSResource;
import mb.resource.text.TextResource;
import mb.resource.text.TextResourceRegistry;
import mb.spoofax.core.platform.DaggerPlatformComponent;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.PlatformPieModule;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.Collections;

public class TestBase {
    public final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
    public final FSResource rootDirectory = new FSResource(fileSystem.getPath("/"));

    public final PlatformComponent platformComponent = DaggerPlatformComponent
        .builder()
        .loggerFactoryModule(new LoggerFactoryModule(new SLF4JLoggerFactory()))
        .platformPieModule(new PlatformPieModule(PieBuilderImpl::new))
        .build();
    public final LoggerFactory loggerFactory = platformComponent.getLoggerFactory();
    public final Logger log = loggerFactory.create(TestBase.class);
    public final TextResourceRegistry textResourceRegistry = platformComponent.getTextResourceRegistry();

    public final CalcComponent languageComponent = DaggerCalcComponent
        .builder()
        .platformComponent(platformComponent)
        .build();
    public final Pie pie = languageComponent.getPie();


    @SafeVarargs public final <T> ArrayList<T> createList(T... items) {
        final ArrayList<T> list = new ArrayList<>();
        Collections.addAll(list, items);
        return list;
    }

    public FSResource createDir(FSResource parent, String relativePath) throws IOException {
        return parent.appendRelativePath(relativePath).createDirectory(true);
    }

    public FSResource createTextFile(FSResource rootDirectory, String text, String relativePath) throws IOException {
        final FSResource resource = rootDirectory.appendRelativePath(relativePath);
        resource.writeString(text, StandardCharsets.UTF_8);
        return resource;
    }

    public FSResource createTextFile(String text, String relativePath) throws IOException {
        return createTextFile(this.rootDirectory, text, relativePath);
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


    public MixedSession newSession() {
        return pie.newSession();
    }
}