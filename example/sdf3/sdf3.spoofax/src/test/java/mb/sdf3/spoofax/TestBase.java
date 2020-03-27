package mb.sdf3.spoofax;

import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.log.slf4j.SLF4JLoggerFactory;
import mb.pie.api.Supplier;
import mb.pie.dagger.PieModule;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.resource.text.TextResource;
import mb.resource.text.TextResourceRegistry;
import mb.sdf3.spoofax.task.Sdf3DesugarTemplates;
import mb.sdf3.spoofax.task.Sdf3Parse;
import mb.sdf3.spoofax.util.DaggerPlatformTestComponent;
import mb.sdf3.spoofax.util.DaggerSdf3TestComponent;
import mb.sdf3.spoofax.util.PlatformTestComponent;
import mb.sdf3.spoofax.util.Sdf3TestComponent;
import mb.spoofax.core.platform.LoggerFactoryModule;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

class TestBase {
    final PlatformTestComponent platformComponent = DaggerPlatformTestComponent
        .builder()
        .loggerFactoryModule(new LoggerFactoryModule(new SLF4JLoggerFactory()))
        .pieModule(new PieModule(PieBuilderImpl::new))
        .build();
    final LoggerFactory loggerFactory = platformComponent.getLoggerFactory();
    final Logger log = loggerFactory.create(TestBase.class);
    final ResourceService resourceService = platformComponent.getResourceService();
    final TextResourceRegistry textResourceRegistry = platformComponent.getTextResourceRegistry();

    final Sdf3TestComponent languageComponent = DaggerSdf3TestComponent
        .builder()
        .platformComponent(platformComponent)
        .sdf3Module(new Sdf3Module())
        .build();
    final Sdf3Parse parse = languageComponent.getParse();
    final Sdf3DesugarTemplates desugarTemplates = languageComponent.getDesugarTemplates();

    final Sdf3Instance languageInstance = languageComponent.getLanguageInstance();


    TextResource createResource(String text, String id) {
        return textResourceRegistry.createResource(text, id);
    }


    Supplier<@Nullable IStrategoTerm> parsedAstSupplier(ResourceKey resourceKey) {
        return parse.createAstSupplier(resourceKey);
    }

    Supplier<@Nullable IStrategoTerm> parsedAstSupplier(Resource resource) {
        return parsedAstSupplier(resource.getKey());
    }


    Supplier<@Nullable IStrategoTerm> desugaredAstSupplier(ResourceKey resourceKey) {
        return desugarTemplates.createSupplier(parsedAstSupplier(resourceKey));
    }

    Supplier<@Nullable IStrategoTerm> desugaredAstSupplier(Resource resource) {
        return desugarTemplates.createSupplier(parsedAstSupplier(resource));
    }
}
