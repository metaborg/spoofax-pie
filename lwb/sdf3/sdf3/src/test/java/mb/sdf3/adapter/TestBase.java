package mb.sdf3.adapter;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.common.result.Result;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.log.slf4j.SLF4JLoggerFactory;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.SerializableFunction;
import mb.pie.api.Supplier;
import mb.pie.api.ValueSupplier;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.text.TextResource;
import mb.resource.text.TextResourceRegistry;
import mb.sdf3.DaggerSdf3Component;
import mb.sdf3.DaggerSdf3ResourcesComponent;
import mb.sdf3.Sdf3Component;
import mb.sdf3.Sdf3ResourcesComponent;
import mb.sdf3.task.Sdf3AnalyzeMulti;
import mb.sdf3.task.Sdf3CreateSpec;
import mb.sdf3.task.Sdf3Desugar;
import mb.sdf3.task.Sdf3Parse;
import mb.sdf3.task.Sdf3Spec;
import mb.sdf3.task.util.Sdf3Util;
import mb.spoofax.core.platform.BaseResourceServiceComponent;
import mb.spoofax.core.platform.BaseResourceServiceModule;
import mb.spoofax.core.platform.DaggerBaseResourceServiceComponent;
import mb.spoofax.core.platform.DaggerPlatformComponent;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.PlatformPieModule;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;

class TestBase {
    final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
    final FSResource rootDirectory = new FSResource(fileSystem.getPath("/"));
    final TextResourceRegistry textResourceRegistry = new TextResourceRegistry();

    final Sdf3ResourcesComponent resourcesComponent = DaggerSdf3ResourcesComponent.create();
    final BaseResourceServiceModule resourceServiceModule = new BaseResourceServiceModule()
        .addRegistry(textResourceRegistry)
        .addRegistriesFrom(resourcesComponent);
    final BaseResourceServiceComponent resourceServiceComponent = DaggerBaseResourceServiceComponent.builder()
        .baseResourceServiceModule(resourceServiceModule)
        .build();

    final PlatformComponent platformComponent = DaggerPlatformComponent.builder()
        .loggerFactoryModule(new LoggerFactoryModule(new SLF4JLoggerFactory()))
        .platformPieModule(new PlatformPieModule(PieBuilderImpl::new))
        .resourceServiceComponent(resourceServiceComponent)
        .build();
    final LoggerFactory loggerFactory = platformComponent.getLoggerFactory();
    final Logger log = loggerFactory.create(TestBase.class);

    final Sdf3Component languageComponent = DaggerSdf3Component.builder()
        .sdf3ResourcesComponent(resourcesComponent)
        .resourceServiceComponent(resourceServiceComponent)
        .platformComponent(platformComponent)
        .build();
    final Sdf3Parse parse = languageComponent.getSdf3Parse();
    final Sdf3Desugar desugar = languageComponent.getSdf3Desugar();
    final Sdf3CreateSpec createSpec = languageComponent.getSdf3CreateSpec();
    final Sdf3AnalyzeMulti analyze = languageComponent.getSdf3AnalyzeMulti();
    final Pie pie = languageComponent.getPie();


    FSResource createTextFile(String text, String relativePath) throws IOException {
        final FSResource resource = rootDirectory.appendRelativePath(relativePath);
        resource.writeString(text, StandardCharsets.UTF_8);
        return resource;
    }

    TextResource createTextResource(String text, String id) {
        return textResourceRegistry.createResource(text, id);
    }


    Supplier<? extends Result<IStrategoTerm, ?>> parsedAstSupplier(ResourceKey resourceKey) {
        return parse.createAstSupplier(resourceKey);
    }

    Supplier<? extends Result<IStrategoTerm, ?>> parsedAstSupplier(Resource resource) {
        return parsedAstSupplier(resource.getKey());
    }


    Supplier<Result<IStrategoTerm, ?>> desugarSupplier(ResourceKey resourceKey) {
        return desugar.createSupplier(parsedAstSupplier(resourceKey));
    }

    Supplier<Result<IStrategoTerm, ?>> desugarSupplier(Resource resource) {
        return desugar.createSupplier(parsedAstSupplier(resource));
    }


    Supplier<? extends Result<Sdf3AnalyzeMulti.SingleFileOutput, ?>> singleFileAnalysisResultSupplier(ResourcePath project, ResourceKey file) {
        return analyze.createSingleFileOutputSupplier(new Sdf3AnalyzeMulti.Input(project, Sdf3Util.createResourceWalker(), Sdf3Util.createResourceMatcher(), desugar.createFunction().mapInput((SerializableFunction<Supplier<String>, Supplier<? extends Result<IStrategoTerm, ?>>>)parse::createRecoverableAstSupplier)), file);
    }

    Supplier<? extends Result<Sdf3AnalyzeMulti.SingleFileOutput, ?>> singleFileAnalysisResultSupplier(Resource file) {
        return singleFileAnalysisResultSupplier(rootDirectory.getPath(), file.getKey());
    }


    Supplier<Sdf3Spec> specSupplier(ResourcePath project, ResourceKey mainFile) {
        return createSpec.createSupplier(new Sdf3CreateSpec.Input(project, mainFile));
    }

    @SafeVarargs
    final Supplier<Sdf3Spec> specSupplier(Supplier<Result<IStrategoTerm, ?>> mainModuleAstSupplier, Supplier<Result<IStrategoTerm, ?>>... modulesAstSuppliers) {
        return new ValueSupplier<>(new Sdf3Spec(mainModuleAstSupplier, modulesAstSuppliers));
    }


    MixedSession newSession() {
        return pie.newSession();
    }
}
