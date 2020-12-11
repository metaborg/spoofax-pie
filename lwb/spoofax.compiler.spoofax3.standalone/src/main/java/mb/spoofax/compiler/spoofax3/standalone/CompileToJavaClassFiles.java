package mb.spoofax.compiler.spoofax3.standalone;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.common.util.ADT;
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
import mb.spoofax.compiler.spoofax3.language.CompilerException;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProjectCompiler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

import javax.inject.Inject;
import javax.tools.Diagnostic;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Value.Enclosing
public class CompileToJavaClassFiles implements TaskDef<CompileToJavaClassFiles.Input, Result<CompileToJavaClassFiles.Output, CompileToJavaClassFiles.CompileException>> {
    private final ResourceService resourceService;
    private final LanguageProjectCompiler languageProjectCompiler;
    private final Spoofax3LanguageProjectCompiler spoofax3LanguageProjectCompiler;
    private final AdapterProjectCompiler adapterProjectCompiler;
    private final CompileJava compileJava;


    @Inject public CompileToJavaClassFiles(
        ResourceService resourceService,
        LanguageProjectCompiler languageProjectCompiler,
        Spoofax3LanguageProjectCompiler spoofax3LanguageProjectCompiler,
        AdapterProjectCompiler adapterProjectCompiler,
        CompileJava compileJava
    ) {
        this.resourceService = resourceService;
        this.languageProjectCompiler = languageProjectCompiler;
        this.spoofax3LanguageProjectCompiler = spoofax3LanguageProjectCompiler;
        this.adapterProjectCompiler = adapterProjectCompiler;
        this.compileJava = compileJava;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Result<Output, CompileException> exec(ExecContext context, Input input) throws Exception {
        final ArrayList<ResourcePath> sourceFiles = new ArrayList<>();
        for(ResourcePath sourcePath : input.sourcePath()) {
            final HierarchicalResource directory = resourceService.getHierarchicalResource(sourcePath);
            if(directory.exists() && directory.isDirectory()) {
                try(final Stream<? extends HierarchicalResource> stream = directory.walk(new TrueResourceWalker(), new PathResourceMatcher(new ExtensionPathMatcher("java")))) {
                    stream.forEach((r) -> sourceFiles.add(r.getPath()));
                }
            }
        }
        final ArrayList<ResourcePath> sourcePath = new ArrayList<>(input.sourcePath());
        final ArrayList<Supplier<?>> suppliers = new ArrayList<>();
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();

        final Task<None> languageProjectCompilerTask = languageProjectCompiler.createTask(input.languageProjectInput());
        context.require(languageProjectCompilerTask);
        suppliers.add(languageProjectCompilerTask.toSupplier());
        sourceFiles.addAll(input.languageProjectInput().javaSourceFiles());

        final Task<Result<KeyedMessages, CompilerException>> spoofax3LanguageProjectCompilerTask = spoofax3LanguageProjectCompiler.createTask(input.spoofax3LanguageProjectInput());
        @SuppressWarnings("ConstantConditions") final Result<KeyedMessages, CompilerException> spoofax3CompilerResult = context.require(spoofax3LanguageProjectCompilerTask);
        suppliers.add(spoofax3LanguageProjectCompilerTask.toSupplier());
        sourcePath.add(input.spoofax3LanguageProjectInput().spoofax3LanguageProject().generatedJavaSourcesDirectory());
        if(spoofax3CompilerResult.isErr()) {
            return Result.ofErr(CompileException.spoofax3LanguageProjectCompilerFail(spoofax3CompilerResult.getErr()));
        } else {
            messagesBuilder.addMessages(spoofax3CompilerResult.get());
        }

        final Task<None> adapterProjectCompilerTask = adapterProjectCompiler.createTask(input.adapterProjectInput());
        context.require(adapterProjectCompilerTask);
        suppliers.add(adapterProjectCompilerTask.toSupplier());
        sourceFiles.addAll(input.adapterProjectInput().javaSourceFiles());

        final ArrayList<CompileJava.Message> javaCompilationMessages = context.require(compileJava, new CompileJava.Input(
            sourceFiles,
            sourcePath,
            new ArrayList<>(input.classPath()),
            new ArrayList<>(input.annotationProcessorPath()),
            input.sourceRelease(),
            input.targetRelease(),
            input.sourceFileOutputDir(),
            input.classFileOutputDir(),
            suppliers
        ));
        if(!addToKeyedMessagesBuilder(javaCompilationMessages, messagesBuilder)) {
            return Result.ofErr(CompileException.javaCompilationFail(messagesBuilder.build()));
        } else {
            return Result.ofOk(Output.builder()
                .addClassPath(input.classFileOutputDir(), input.spoofax3LanguageProjectInput().spoofax3LanguageProject().generatedResourcesDirectory())
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


    @Value.Immutable public interface Input extends Serializable {
        static CompileToJavaClassFilesData.Input.Builder builder() { return CompileToJavaClassFilesData.Input.builder(); }


        /// Sub-inputs

        LanguageProjectCompiler.Input languageProjectInput();

        Spoofax3LanguageProjectCompiler.Input spoofax3LanguageProjectInput();

        AdapterProjectCompiler.Input adapterProjectInput();


        // Java compilation

        @Value.Default default List<ResourcePath> sourcePath() {
            final ArrayList<ResourcePath> additionalSourcePaths = new ArrayList<>();
            additionalSourcePaths.add(adapterProjectInput().adapterProject().project().srcMainDirectory().appendSegment("java"));
            return additionalSourcePaths;
        }

        List<File> classPath();

        List<File> annotationProcessorPath();

        @Value.Default default String sourceRelease() {
            return "8";
        }

        @Value.Default default String targetRelease() {
            return "8";
        }

        @Value.Default default ResourcePath sourceFileOutputDir() {
            return adapterProjectInput().adapterProject().project().buildGeneratedSourcesAnnotationProcessorJavaMainDirectory();
        }

        @Value.Default default ResourcePath classFileOutputDir() {
            return adapterProjectInput().adapterProject().project().buildClassesJavaMainDirectory();
        }
    }

    @Value.Immutable public interface Output extends Serializable {
        static CompileToJavaClassFilesData.Output.Builder builder() { return CompileToJavaClassFilesData.Output.builder(); }

        List<ResourcePath> classPath();

        KeyedMessages messages();
    }

    @ADT public static abstract class CompileException extends Exception {
        public interface Cases<R> {
            R walkSourceFilesFail(IOException ioException);

            R spoofax3LanguageProjectCompilerFail(CompilerException compilerException);

            R javaCompilationFail(KeyedMessages keyedMessages);
        }

        public static CompileException walkSourceFilesFail(IOException cause) {
            return withCause(CompileExceptions.walkSourceFilesFail(cause), cause);
        }

        public static CompileException spoofax3LanguageProjectCompilerFail(CompilerException cause) {
            return withCause(CompileExceptions.spoofax3LanguageProjectCompilerFail(cause), cause);
        }

        public static CompileException javaCompilationFail(KeyedMessages keyedMessages) {
            return CompileExceptions.javaCompilationFail(keyedMessages);
        }


        private static CompileException withCause(CompileException e, Exception cause) {
            e.initCause(cause);
            return e;
        }


        public abstract <R> R match(CompileException.Cases<R> cases);

        public static CompileExceptions.CasesMatchers.TotalMatcher_WalkSourceFilesFail cases() {
            return CompileExceptions.cases();
        }

        public CompileExceptions.CaseOfMatchers.TotalMatcher_WalkSourceFilesFail caseOf() {
            return CompileExceptions.caseOf(this);
        }

        public Optional<String> getSubMessage() {
            return cases()
                .walkSourceFilesFail(IOException::getMessage)
                .spoofax3LanguageProjectCompilerFail(CompilerException::getMessage)
                .otherwiseEmpty()
                .apply(this);
        }

        public Optional<KeyedMessages> getSubMessages() {
            return cases()
                .spoofax3LanguageProjectCompilerFail(CompilerException::getSubMessages)
                .javaCompilationFail(Optional::of)
                .otherwise_(Optional.empty())
                .apply(this);
        }

        public @Nullable Throwable getSubCause() {
            return cases()
                .walkSourceFilesFail(IOException::getCause)
                .spoofax3LanguageProjectCompilerFail(CompilerException::getCause)
                .otherwise_(null)
                .apply(this);
        }


        @Override public @NonNull String getMessage() {
            return cases()
                .walkSourceFilesFail((e) -> "Walking source file tree failed")
                .spoofax3LanguageProjectCompilerFail((e) -> "Spoofax 3 language project compiler failed")
                .javaCompilationFail((e) -> "Java compilation failed")
                .apply(this);
        }

        @Override public Throwable fillInStackTrace() {
            return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
        }


        @Override public abstract int hashCode();

        @Override public abstract boolean equals(@Nullable Object obj);
    }
}
