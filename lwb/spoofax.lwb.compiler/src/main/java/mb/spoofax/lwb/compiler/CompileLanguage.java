package mb.spoofax.lwb.compiler;

import mb.cfg.CompileLanguageInput;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.cfg.task.CfgToObject;
import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.STask;
import mb.pie.api.Stateful1Supplier;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.pie.task.java.CompileJava;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.language.LanguageProjectCompiler;
import mb.spoofax.compiler.platform.EclipseProjectCompiler;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * Fully compiles a language by running the {@link LanguageProjectCompiler language project compiler}, {@link
 * CompileLanguageSpecification language specification compiler}, {@link AdapterProjectCompiler adapter project
 * compiler}, and the {@link CompileJava Java compiler}. This fully compiles a language from its sources to Java class
 * files and corresponding resources.
 *
 * Takes an {@link CompileLanguage.Args} as input, which contains the root directory that is used to find the CFG file
 * configuring the various compilers, and fields to add additional Java class and annotation processor paths.
 *
 * Produces a {@link Result} that is either an {@link Output} or a {@link CompileLanguageException} when compilation
 * fails. The {@link Output} contains all {@link KeyedMessages messages} produced during compilation, and the Java class
 * path that can be used to run the language, dynamically load it, or to package it into a JAR file.
 */
@Value.Enclosing
public class CompileLanguage implements TaskDef<CompileLanguage.Args, Result<CompileLanguage.Output, CompileLanguageException>> {
    @Value.Immutable
    public interface Args extends Serializable {
        class Builder extends CompileLanguageData.Args.Builder {}

        static Builder builder() { return new Builder(); }

        ResourcePath rootDirectory();

        List<File> additionalJavaClassPath();

        List<File> additionalJavaAnnotationProcessorPath();
    }

    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends CompileLanguageData.Output.Builder {}

        static Builder builder() { return new Builder(); }

        List<ResourcePath> classPath();

        KeyedMessages messages();
    }


    private final ResourceService resourceService;
    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;
    private final LanguageProjectCompiler languageProjectCompiler;
    private final CompileLanguageSpecification compileLanguage;
    private final AdapterProjectCompiler adapterProjectCompiler;
    private final EclipseProjectCompiler eclipseProjectCompiler;
    private final CompileJava compileJava;


    @Inject public CompileLanguage(
        ResourceService resourceService,
        CfgRootDirectoryToObject cfgRootDirectoryToObject,
        LanguageProjectCompiler languageProjectCompiler,
        CompileLanguageSpecification compileLanguage,
        AdapterProjectCompiler adapterProjectCompiler,
        EclipseProjectCompiler eclipseProjectCompiler,
        CompileJava compileJava
    ) {
        this.resourceService = resourceService;
        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;
        this.languageProjectCompiler = languageProjectCompiler;
        this.compileLanguage = compileLanguage;
        this.adapterProjectCompiler = adapterProjectCompiler;
        this.eclipseProjectCompiler = eclipseProjectCompiler;
        this.compileJava = compileJava;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<Output, CompileLanguageException> exec(ExecContext context, Args args) {
        final ResourcePath rootDirectory = args.rootDirectory();
        final Task<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>> cfgTask = cfgRootDirectoryToObject.createTask(rootDirectory);
        final STask<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>> cfgSupplier = cfgTask.toSupplier();
        final Result<CfgToObject.Output, CfgRootDirectoryToObjectException> cfgResult = context.require(cfgTask);
        if(cfgResult.isErr()) {
            // noinspection ConstantConditions (error is present)
            return Result.ofErr(CompileLanguageException.getConfigurationFail(cfgResult.getErr()));
        }

        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        @SuppressWarnings("ConstantConditions") final CfgToObject.Output cfgOutput = cfgResult.get();
        // noinspection ConstantConditions (value is present)
        messagesBuilder.addMessages(cfgOutput.messages);

        final CompileLanguageInput input = cfgOutput.compileLanguageInput;
        final CompileJava.Input.Builder compileJavaInputBuilder = CompileJava.Input.builder().key(args.rootDirectory());


        final Task<Result<None, ?>> languageProjectCompilerTask = languageProjectCompiler.createTask(new LanguageProjectInputSupplier(cfgSupplier));
        // TODO: check result? It can only fail due to a CFG failure atm.
        context.require(languageProjectCompilerTask);
        compileJavaInputBuilder.addOriginTasks(languageProjectCompilerTask.toSupplier());

        final Task<Result<CompileLanguageSpecification.Output, CompileLanguageSpecificationException>> languageSpecificationCompilerTask = compileLanguage.createTask(rootDirectory);
        final Result<CompileLanguageSpecification.Output, CompileLanguageSpecificationException> spoofax3CompilerResult = context.require(languageSpecificationCompilerTask);
        compileJavaInputBuilder.addSourceTasks(new LanguageSpecificationJavaSourcesSupplier(languageSpecificationCompilerTask.toSupplier()));
        if(spoofax3CompilerResult.isErr()) {
            // noinspection ConstantConditions (error is present)
            return Result.ofErr(CompileLanguageException.compileLanguageFail(spoofax3CompilerResult.getErr()));
        } else {
            // noinspection ConstantConditions (value is present)
            final CompileLanguageSpecification.Output output = spoofax3CompilerResult.get();
            //noinspection ConstantConditions (value is present)
            messagesBuilder.addMessages(output.messages());
        }

        final Task<Result<None, ?>> adapterProjectCompilerTask = adapterProjectCompiler.createTask(new AdapterProjectInputSupplier(cfgSupplier));
        // TODO: check result? It can only fail due to a CFG failure atm.
        context.require(adapterProjectCompilerTask);
        compileJavaInputBuilder.addOriginTasks(adapterProjectCompilerTask.toSupplier());

        final Task<Result<None, ?>> eclipseProjectCompilerTask = eclipseProjectCompiler.createTask(new EclipseProjectInputSupplier(cfgSupplier));
        // TODO: check result? It can only fail due to a CFG failure atm.
        context.require(eclipseProjectCompilerTask);
        compileJavaInputBuilder.addOriginTasks(eclipseProjectCompilerTask.toSupplier());

        compileJavaInputBuilder
            .addSourceTasks(new JavaSourcesSupplier(cfgSupplier))
            .addAllClassPaths(input.javaClassPaths())
            .addAllClassPaths(args.additionalJavaClassPath())
            .addAllAnnotationProcessorPaths(input.javaAnnotationProcessorPaths())
            .addAllAnnotationProcessorPaths(args.additionalJavaAnnotationProcessorPath())
            .release(input.javaRelease())
            .sourceFileOutputDirectory(input.javaSourceFileOutputDirectory())
            .classFileOutputDirectory(input.javaClassFileOutputDirectory())
            .reportWarnings(false)
        ;
        final KeyedMessages javaCompilationMessages = context.require(compileJava, compileJavaInputBuilder.build());
        messagesBuilder.addMessages(javaCompilationMessages);
        if(javaCompilationMessages.containsError()) {
            return Result.ofErr(CompileLanguageException.javaCompilationFail(messagesBuilder.build()));
        }

        return Result.ofOk(Output.builder()
            .addClassPath(input.javaClassFileOutputDirectory())
            .addAllClassPath(input.resourcePaths())
            .messages(messagesBuilder.build())
            .build()
        );
    }

    @Override public Serializable key(Args input) {
        return input.rootDirectory();
    }


    private static class LanguageProjectInputSupplier extends Stateful1Supplier<STask<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>>, Result<LanguageProjectCompiler.Input, ?>> {
        public LanguageProjectInputSupplier(STask<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>> state) {
            super(state);
        }

        @Override public Result<LanguageProjectCompiler.Input, ?> get(ExecContext context) {
            // OPTO: set output stamper
            return context.require(state).map(o -> o.compileLanguageInput.languageProjectInput());
        }
    }

    private static class AdapterProjectInputSupplier extends Stateful1Supplier<STask<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>>, Result<AdapterProjectCompiler.Input, ?>> {
        public AdapterProjectInputSupplier(STask<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>> state) {
            super(state);
        }

        @Override public Result<AdapterProjectCompiler.Input, ?> get(ExecContext context) {
            // OPTO: set output stamper
            return context.require(state).map(o -> o.compileLanguageInput.adapterProjectInput());
        }
    }

    private static class EclipseProjectInputSupplier extends Stateful1Supplier<STask<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>>, Result<Option<EclipseProjectCompiler.Input>, ?>> {
        public EclipseProjectInputSupplier(STask<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>> state) {
            super(state);
        }

        @Override public Result<Option<EclipseProjectCompiler.Input>, ?> get(ExecContext context) {
            // OPTO: set output stamper
            return context.require(state).map(o -> Option.ofOptional(o.compileLanguageInput.eclipseProjectInput()));
        }
    }

    private static class LanguageSpecificationJavaSourcesSupplier extends Stateful1Supplier<STask<Result<CompileLanguageSpecification.Output, CompileLanguageSpecificationException>>, Result<CompileJava.Sources, ?>> {
        public LanguageSpecificationJavaSourcesSupplier(STask<Result<CompileLanguageSpecification.Output, CompileLanguageSpecificationException>> state) {
            super(state);
        }

        @Override public Result<CompileJava.Sources, ?> get(ExecContext context) {
            // OPTO: set output stamper
            return context.require(state).map(o -> CompileJava.Sources.builder().addAllSourceFiles(o.providedJavaFiles()).build());
        }
    }

    private static class JavaSourcesSupplier extends Stateful1Supplier<STask<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>>, Result<CompileJava.Sources, ?>> {
        public JavaSourcesSupplier(STask<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>> state) {
            super(state);
        }

        @Override public Result<CompileJava.Sources, ?> get(ExecContext context) {
            // OPTO: set output stamper
            return context.require(state).map(o -> CompileJava.Sources.builder()
                .addAllSourceFiles(o.compileLanguageInput.javaSourceFiles())
                .sourceFilesFromPaths(o.compileLanguageInput.userJavaSourcePaths())
                .sourcePaths(o.compileLanguageInput.javaSourcePaths())
                .packagePaths(o.compileLanguageInput.javaSourceDirectoryPaths())
                .build()
            );
        }
    }
}

