package mb.sdf3;

import mb.log.api.LoggerFactory;
import mb.resource.ResourceService;
import mb.resource.url.URLPath;
import mb.resource.url.URLResource;
import mb.spoofax.compiler.interfaces.spoofaxcore.StrategoRuntimeBuilderFactory;
import mb.stratego.common.StrategoIOAgent;
import mb.stratego.common.StrategoRuntimeBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

public class Sdf3ManualStrategoRuntimeBuilderFactory implements StrategoRuntimeBuilderFactory {
    @Override
    public StrategoRuntimeBuilder create(LoggerFactory loggerFactory, ResourceService resourceService) {
        // TODO: go back to extending StrategoRuntimeBuilderFactory once classloader resource filesystem works.
        final StrategoRuntimeBuilder builder = new StrategoRuntimeBuilder(loggerFactory, resourceService);
        builder.addInteropRegistererByReflection("org.metaborg.meta.lang.template.strategies.InteropRegisterer");
        {
            final String resource = "mb/sdf3/target/metaborg/stratego.ctree";
            final @Nullable URL locationURL = Sdf3ManualStrategoRuntimeBuilderFactory.class.getClassLoader().getResource(resource);
            if(locationURL == null) {
                throw new RuntimeException("Cannot create Stratego runtime builder; cannot find resource '" + resource + "' in classloader resources");
            }
            builder.addCtree(new URLResource(locationURL));
        }
        builder.withJarParentClassLoader(Sdf3StrategoRuntimeBuilderFactory.class.getClassLoader());
        builder.addLibrary(new mb.constraint.common.stratego.ConstraintPrimitiveLibrary(resourceService));
        builder.addLibrary(new mb.nabl2.common.NaBL2PrimitiveLibrary());
        {
            builder.addLibrary(new mb.statix.common.StatixPrimitiveLibrary());
            builder.addLibrary(new mb.spoofax2.common.primitive.Spoofax2PrimitiveLibrary(loggerFactory, resourceService));
            final String resource = "mb/sdf3/";

            // TODO: remove hack that gets last URL
            @Nullable URL locationURL = null;
            try {
                final java.util.Enumeration<URL> urls = Sdf3ManualStrategoRuntimeBuilderFactory.class.getClassLoader().getResources("mb/sdf3/");
                while(urls.hasMoreElements()) {
                    locationURL = urls.nextElement();
                }
            } catch(java.io.IOException e) {
                e.printStackTrace();
            }
            //final @Nullable URL locationURL = Sdf3ManualStrategoRuntimeBuilderFactory.class.getClassLoader().getResource(resource);

            if(locationURL == null) {
                throw new RuntimeException("Cannot create Spoofax2Context; cannot find resource '" + resource + "' in classloader resources");
            }
            final URLPath path = new URLPath(locationURL);
            builder.addContextObject(new mb.spoofax2.common.primitive.generic.Spoofax2Context("org.metaborg", "sdf3", "develop-SNAPSHOT", path, resourceService.toResourceKeyString(path)));
        }

        builder.addLibrary(new Sdf3PrimitiveLibrary());

        // TODO: remove hack that gets last URL
        // TODO: move this to generated code, as all languages can benefit from this.
        final StrategoIOAgent ioAgent = new StrategoIOAgent(loggerFactory, resourceService);
        @Nullable URL url = null;
        try {
            final Enumeration<URL> urls = Sdf3ManualStrategoRuntimeBuilderFactory.class.getClassLoader().getResources("mb/sdf3/");
            while(urls.hasMoreElements()) {
                 url = urls.nextElement();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

        //final @Nullable URL url = Sdf3ManualStrategoRuntimeBuilderFactory.class.getClassLoader().getResource("mb/sdf3/");
        if(url == null) {
            throw new RuntimeException("Cannot create Stratego runtime builder; cannot find resource 'mb/sdf3/' in classloader resources");
        }
        final URLResource urlResource = new URLResource(url);
        ioAgent.setAbsoluteDefinitionDir(urlResource);
        builder.withIoAgent(ioAgent);

        return builder;
    }
}
