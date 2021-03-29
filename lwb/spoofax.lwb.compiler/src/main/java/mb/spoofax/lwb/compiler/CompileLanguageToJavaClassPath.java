package mb.spoofax.lwb.compiler;

import mb.cfg.CompileLanguageToJavaClassPathInput;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.cfg.task.CfgToObject;
import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.Supplier;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.pie.task.java.CompileJava;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.PathResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionPathMatcher;
import mb.resource.hierarchical.walk.TrueResourceWalker;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.language.LanguageProjectCompiler;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

import javax.inject.Inject;
import javax.tools.Diagnostic;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Stream;

/**
 * Fully compiles a language by running the {@link LanguageProjectCompiler language project compiler}, {@link
 * CompileLanguage language specification compiler}, {@link AdapterProjectCompiler adapter project compiler}, and the
 * {@link CompileJava Java compiler}. This fully compiles a language from its sources to Java class files and
 * corresponding resources.
 *
 * Takes an {@link CompileLanguageToJavaClassPath.Args} as input, which contains the root directory that is used to find
 * the CFG file configuring the various compilers, and fields to add additional Java class and annotation processor
 * paths.
 *
 * Produces a {@link Result} that is either an {@link Output} or a {@link CompileLanguageToJavaClassPathException} when
 * compilation fails. The {@link Output} contains all {@link KeyedMessages messages} produced during compilation, and
 * the Java class path that can be used to run the language, dynamically load it, or to package it into a JAR file.
 */
@Value.Enclosing
public class CompileLanguageToJavaClassPath implements TaskDef<CompileLanguageToJavaClassPath.Args, Result<CompileLanguageToJavaClassPath.Output, CompileLanguageToJavaClassPathException>> {
    public static class Args implements Serializable {
        public final ResourcePath rootDirectory;
        public final ListView<File> additionalJavaClassPath;
        public final ListView<File> additionalJavaAnnotationProcessorPath;

        public Args(ResourcePath rootDirectory, ListView<File> additionalJavaClassPath, ListView<File> additionalJavaAnnotationProcessorPath) {
            this.rootDirectory = rootDirectory;
            this.additionalJavaClassPath = additionalJavaClassPath;
            this.additionalJavaAnnotationProcessorPath = additionalJavaAnnotationProcessorPath;
        }

        public Args(ResourcePath rootDirectory, ListView<File> additionalJavaClassPath) {
            this(rootDirectory, additionalJavaClassPath, ListView.of());
        }

        public Args(ResourcePath rootDirectory) {
            this(rootDirectory, ListView.of());
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Args args = (Args)o;
            if(!rootDirectory.equals(args.rootDirectory)) return false;
            if(!additionalJavaClassPath.equals(args.additionalJavaClassPath)) return false;
            return additionalJavaAnnotationProcessorPath.equals(args.additionalJavaAnnotationProcessorPath);
        }

        @Override public int hashCode() {
            int result = rootDirectory.hashCode();
            result = 31 * result + additionalJavaClassPath.hashCode();
            result = 31 * result + additionalJavaAnnotationProcessorPath.hashCode();
            return result;
        }

        @Override public String toString() {
            return "CompileLanguageToJavaClassPath$Args{" +
                "rootDirectory=" + rootDirectory +
                ", additionalJavaClassPath=" + additionalJavaClassPath +
                ", additionalJavaAnnotationProcessorPath=" + additionalJavaAnnotationProcessorPath +
                '}';
        }
    }

    private final ResourceService resourceService;
    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;
    private final LanguageProjectCompiler languageProjectCompiler;
    private final CompileLanguage compileLanguage;
    private final AdapterProjectCompiler adapterProjectCompiler;
    private final CompileJava compileJava;


    @Inject public CompileLanguageToJavaClassPath(
        ResourceService resourceService,
        CfgRootDirectoryToObject cfgRootDirectoryToObject,
        LanguageProjectCompiler languageProjectCompiler,
        CompileLanguage compileLanguage,
        AdapterProjectCompiler adapterProjectCompiler,
        CompileJava compileJava
    ) {
        this.resourceService = resourceService;
        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;
        this.languageProjectCompiler = languageProjectCompiler;
        this.compileLanguage = compileLanguage;
        this.adapterProjectCompiler = adapterProjectCompiler;
        this.compileJava = compileJava;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<Output, CompileLanguageToJavaClassPathException> exec(ExecContext context, CompileLanguageToJavaClassPath.Args args) {
        final ResourcePath rootDirectory = args.rootDirectory;
        final Result<CfgToObject.Output, CfgRootDirectoryToObjectException> cfgResult = context.require(cfgRootDirectoryToObject, rootDirectory);
        if(cfgResult.isErr()) {
            // noinspection ConstantConditions (error is present)
            return Result.ofErr(CompileLanguageToJavaClassPathException.getConfigurationFail(cfgResult.getErr()));
        }

        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        @SuppressWarnings("ConstantConditions") final CfgToObject.Output cfgOutput = cfgResult.get();
        // noinspection ConstantConditions (value is present)
        messagesBuilder.addMessages(cfgOutput.messages);

        final CompileLanguageToJavaClassPathInput input = cfgOutput.compileLanguageToJavaClassPathInput;

        final ArrayList<ResourcePath> javaSourceFiles = new ArrayList<>();
        // Add all Java source files from the user-defined source path.
        for(ResourcePath javaSourcePath : input.javaSourcePath()) {
            final HierarchicalResource directory = resourceService.getHierarchicalResource(javaSourcePath);
            try {
                if(directory.exists() && directory.isDirectory()) {
                    try(final Stream<? extends HierarchicalResource> stream = directory.walk(new TrueResourceWalker(), new PathResourceMatcher(new ExtensionPathMatcher("java")))) {
                        stream.forEach((r) -> javaSourceFiles.add(r.getPath()));
                    }
                }
            } catch(IOException e) {
                return Result.ofErr(CompileLanguageToJavaClassPathException.walkJavaSourceFilesFail(e));
            }
        }
        final LinkedHashSet<ResourcePath> javaSourcePath = new LinkedHashSet<>(input.javaSourcePath()); // LinkedHashSet to preserve insertion order.
        final ArrayList<Supplier<?>> suppliers = new ArrayList<>();

        // OPTO: pass in supplier to prevent dependency on large input?
        final Task<None> languageProjectCompilerTask = languageProjectCompiler.createTask(input.languageProjectInput());
        context.require(languageProjectCompilerTask);
        suppliers.add(languageProjectCompilerTask.toSupplier());
        javaSourceFiles.addAll(input.languageProjectInput().javaSourceFiles());
        javaSourcePath.addAll(input.languageProjectInput().javaSourcePaths());

        final Task<Result<KeyedMessages, CompileLanguage.CompileException>> languageSpecificationCompilerTask = compileLanguage.createTask(rootDirectory);
        final Result<KeyedMessages, CompileLanguage.CompileException> spoofax3CompilerResult = context.require(languageSpecificationCompilerTask);
        suppliers.add(languageSpecificationCompilerTask.toSupplier());
        javaSourceFiles.addAll(input.compileLanguageInput().javaSourceFiles());
        javaSourcePath.addAll(input.compileLanguageInput().javaSourcePaths());
        if(spoofax3CompilerResult.isErr()) {
            // noinspection ConstantConditions (error is present)
            return Result.ofErr(CompileLanguageToJavaClassPathException.compileLanguageFail(spoofax3CompilerResult.getErr()));
        } else {
            // noinspection ConstantConditions (value is present)
            messagesBuilder.addMessages(spoofax3CompilerResult.get());
        }

        // OPTO: pass in supplier to prevent dependency on large input?
        final Task<None> adapterProjectCompilerTask = adapterProjectCompiler.createTask(input.adapterProjectInput());
        context.require(adapterProjectCompilerTask);
        suppliers.add(adapterProjectCompilerTask.toSupplier());
        javaSourceFiles.addAll(input.adapterProjectInput().javaSourceFiles());
        javaSourcePath.addAll(input.adapterProjectInput().javaSourcePaths());

        final ArrayList<File> classPath = new ArrayList<>(input.javaClassPath());
        args.additionalJavaClassPath.addAllTo(classPath);
        final ArrayList<File> annotationProcessorPath = new ArrayList<>(input.javaAnnotationProcessorPath());
        args.additionalJavaAnnotationProcessorPath.addAllTo(annotationProcessorPath);

        final ArrayList<CompileJava.Message> javaCompilationMessages = context.require(compileJava, new CompileJava.Input(
            javaSourceFiles,
            new ArrayList<>(javaSourcePath),
            classPath,
            annotationProcessorPath,
            input.javaRelease(),
            input.javaRelease(),
            input.javaSourceFileOutputDirectory(),
            input.javaClassFileOutputDirectory(),
            suppliers
        ));
        if(!addToKeyedMessagesBuilder(javaCompilationMessages, messagesBuilder)) {
            return Result.ofErr(CompileLanguageToJavaClassPathException.javaCompilationFail(messagesBuilder.build()));
        } else {
            return Result.ofOk(Output.builder()
                .addClassPath(input.javaClassFileOutputDirectory(), input.compileLanguageInput().compileLanguageShared().generatedResourcesDirectory())
                .messages(messagesBuilder.build())
                .build()
            );
        }
    }

    private boolean addToKeyedMessagesBuilder(ArrayList<CompileJava.Message> messages, KeyedMessagesBuilder messagesBuilder) {
        boolean success = true;
        for(final CompileJava.Message message : messages) {
            final Severity severity = toSeverity(message.kind);
            success = success && (severity != Severity.Error);
            final @Nullable ResourceKey resource = message.resource;
            if(resource != null) {
                final @Nullable Region region;
                if(message.startOffset != Diagnostic.NOPOS && message.endOffset != Diagnostic.NOPOS) {
                    region = Region.fromOffsets((int)message.startOffset, (int)message.endOffset, (int)message.line);
                } else {
                    region = null;
                }
                messagesBuilder.addMessage(message.text, severity, resource, region);
            } else {
                messagesBuilder.addMessage(message.text, severity);
            }
        }
        return success;
    }

    private Severity toSeverity(Diagnostic.Kind kind) {
        switch(kind) {
            case ERROR:
                return Severity.Error;
            case WARNING:
            case MANDATORY_WARNING:
                return Severity.Warning;
            case NOTE:
                return Severity.Info;
            case OTHER:
            default:
                return Severity.Debug;
        }
    }


    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends CompileLanguageToJavaClassPathData.Output.Builder {}

        static Builder builder() { return new Builder(); }

        List<ResourcePath> classPath();

        KeyedMessages messages();
    }

}
