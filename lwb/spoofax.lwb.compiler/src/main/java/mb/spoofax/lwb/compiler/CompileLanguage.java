package mb.spoofax.lwb.compiler;

import mb.cfg.CompileLanguageInput;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.cfg.task.CfgToObject;
import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.pie.task.java.CompileJava;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.PathMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.language.LanguageProjectCompiler;
import mb.spoofax.compiler.platform.EclipseProjectCompiler;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

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
        final Result<CfgToObject.Output, CfgRootDirectoryToObjectException> cfgResult = context.require(cfgRootDirectoryToObject, rootDirectory);
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

        // OPTO: pass in supplier to prevent dependency on large input?
        final Task<None> languageProjectCompilerTask = languageProjectCompiler.createTask(input.languageProjectInput());
        context.require(languageProjectCompilerTask);
        compileJavaInputBuilder.addOriginTasks(languageProjectCompilerTask.toSupplier());

        final Task<Result<KeyedMessages, CompileLanguageSpecificationException>> languageSpecificationCompilerTask = compileLanguage.createTask(rootDirectory);
        final Result<KeyedMessages, CompileLanguageSpecificationException> spoofax3CompilerResult = context.require(languageSpecificationCompilerTask);
        compileJavaInputBuilder.addOriginTasks(languageSpecificationCompilerTask.toSupplier());
        if(spoofax3CompilerResult.isErr()) {
            // noinspection ConstantConditions (error is present)
            return Result.ofErr(CompileLanguageException.compileLanguageFail(spoofax3CompilerResult.getErr()));
        } else {
            // noinspection ConstantConditions (value is present)
            messagesBuilder.addMessages(spoofax3CompilerResult.get());
        }

        // OPTO: pass in supplier to prevent dependency on large input?
        final Task<None> adapterProjectCompilerTask = adapterProjectCompiler.createTask(input.adapterProjectInput());
        context.require(adapterProjectCompilerTask);
        compileJavaInputBuilder.addOriginTasks(adapterProjectCompilerTask.toSupplier());

        input.eclipseProjectInput().ifPresent(eclipseProjectInput -> {
            final Task<EclipseProjectCompiler.Output> task = eclipseProjectCompiler.createTask(eclipseProjectInput);
            context.require(task);
            compileJavaInputBuilder.addOriginTasks(task.toSupplier());
        });

        for(ResourcePath javaSourcePath : input.userJavaSourcePaths()) { // Add all Java source files from the user-defined source path.
            final HierarchicalResource directory = resourceService.getHierarchicalResource(javaSourcePath);
            try {
                if(directory.exists() && directory.isDirectory()) {
                    try(final Stream<? extends HierarchicalResource> stream = directory.walk(
                        ResourceWalker.ofPath(PathMatcher.ofNoHidden()),
                        ResourceMatcher.ofPath(PathMatcher.ofExtension("java")).and(ResourceMatcher.ofFile())
                    )) {
                        stream.forEach((r) -> compileJavaInputBuilder.addSourceFiles(r.getPath()));
                    }
                }
            } catch(IOException e) {
                return Result.ofErr(CompileLanguageException.walkJavaSourceFilesFail(e));
            }
        }

        compileJavaInputBuilder
            .addAllSourceFiles(input.javaSourceFiles())
            .sourcePaths(input.javaSourcePaths())
            .addAllClassPaths(input.javaClassPaths())
            .addAllClassPaths(args.additionalJavaClassPath())
            .addAllAnnotationProcessorPaths(input.javaAnnotationProcessorPaths())
            .addAllAnnotationProcessorPaths(args.additionalJavaAnnotationProcessorPath())
            .release(input.javaRelease())
            .sourceFileOutputDirectory(input.javaSourceFileOutputDirectory())
            .classFileOutputDirectory(input.javaClassFileOutputDirectory())
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
}
