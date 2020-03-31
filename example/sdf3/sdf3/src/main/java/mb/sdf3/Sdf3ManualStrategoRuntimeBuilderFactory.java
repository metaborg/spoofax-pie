package mb.sdf3;

import mb.log.api.LoggerFactory;
import mb.resource.ResourceService;
import mb.resource.url.URLResource;
import mb.stratego.common.StrategoIOAgent;
import mb.stratego.common.StrategoRuntimeBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.URL;

public class Sdf3ManualStrategoRuntimeBuilderFactory extends Sdf3StrategoRuntimeBuilderFactory {
    @Override
    public StrategoRuntimeBuilder create(LoggerFactory loggerFactory, ResourceService resourceService) {
        final StrategoRuntimeBuilder builder = super.create(loggerFactory, resourceService);
        builder.addLibrary(new Sdf3PrimitiveLibrary());

        // TODO: move this to generated code, as all languages can benefit from this.
        final StrategoIOAgent ioAgent = new StrategoIOAgent(loggerFactory, resourceService);
        final @Nullable URL url = Sdf3ManualStrategoRuntimeBuilderFactory.class.getClassLoader().getResource("mb/sdf3/");
        if(url == null) {
            throw new RuntimeException("Cannot create Stratego runtime builder; cannot find resource 'mb/sdf3/' in classloader resources");
        }
        final URLResource urlResource = new URLResource(url);
        ioAgent.setAbsoluteDefinitionDir(urlResource);
        builder.withIoAgent(ioAgent);

        return builder;
    }
}
