package mb.spoofax.lwb.compiler.metalang;

import mb.cfg.task.CfgToObjectExceptions;
import mb.common.message.HasOptionalMessages;
import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.common.util.ADT;
import mb.common.util.ListView;
import mb.esv.task.EsvCheckMulti;
import mb.esv.task.EsvCompile;
import mb.esv.task.EsvParse;
import mb.esv.util.EsvUtil;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.libspoofax2.LibSpoofax2ClassLoaderResources;
import mb.libspoofax2.LibSpoofax2Exports;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.SerializableFunction;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.api.ValueSupplier;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.pie.task.archive.UnarchiveFromJar;
import mb.resource.ReadableResource;
import mb.resource.ResourceKey;
import mb.resource.WritableResource;
import mb.resource.classloader.ClassLoaderResourceLocations;
import mb.resource.classloader.JarFileWithPath;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.lwb.compiler.CompileLanguageToJavaClassPathExceptions;
import mb.spoofx.lwb.compiler.cfg.metalang.CompileEsvInput;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Value.Enclosing
public class CompileEsv implements TaskDef<CompileEsv.Args, Result<KeyedMessages, CompileEsv.EsvCompileException>> {
    public static class Args implements Serializable {
        public final CompileEsvInput input;
        public final ArrayList<Supplier<Result<IStrategoTerm, ?>>> esvAdditionalAstSuppliers;

        public Args(CompileEsvInput input, ArrayList<Supplier<Result<IStrategoTerm, ?>>> esvAdditionalAstSuppliers) {
            this.input = input;
            this.esvAdditionalAstSuppliers = esvAdditionalAstSuppliers;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Args args = (Args)o;
            return input.equals(args.input) && esvAdditionalAstSuppliers.equals(args.esvAdditionalAstSuppliers);
        }

        @Override public int hashCode() {
            return Objects.hash(input, esvAdditionalAstSuppliers);
        }
    }


    private final EsvParse parse;
    private final EsvCheckMulti check;
    private final EsvCompile compile;

    private final UnarchiveFromJar unarchiveFromJar;
    private final LibSpoofax2ClassLoaderResources libSpoofax2ClassLoaderResources;

    @Inject public CompileEsv(
        EsvParse parse,
        EsvCheckMulti check,
        EsvCompile compile,
        UnarchiveFromJar unarchiveFromJar,
        LibSpoofax2ClassLoaderResources libSpoofax2ClassLoaderResources
    ) {
        this.parse = parse;
        this.check = check;
        this.compile = compile;
        this.unarchiveFromJar = unarchiveFromJar;
        this.libSpoofax2ClassLoaderResources = libSpoofax2ClassLoaderResources;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<KeyedMessages, EsvCompileException> exec(ExecContext context, Args args) throws Exception {
        final CompileEsvInput input = args.input;

        // Check main file, root directory, and include directories.
        final HierarchicalResource mainFile = context.require(input.esvMainFile(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainFile.exists() || !mainFile.isFile()) {
            return Result.ofErr(EsvCompileException.mainFileFail(mainFile.getPath()));
        }
        final HierarchicalResource rootDirectory = context.require(input.esvRootDirectory(), ResourceStampers.<HierarchicalResource>exists());
        if(!rootDirectory.exists() || !rootDirectory.isDirectory()) {
            return Result.ofErr(EsvCompileException.rootDirectoryFail(rootDirectory.getPath()));
        }
        for(ResourcePath includeDirectoryPath : input.esvIncludeDirs()) {
            final HierarchicalResource includeDirectory = context.require(includeDirectoryPath, ResourceStampers.<HierarchicalResource>exists());
            if(!includeDirectory.exists() || !includeDirectory.isDirectory()) {
                return Result.ofErr(EsvCompileException.includeDirectoryFail(includeDirectoryPath));
            }
        }

        // Determine libspoofax2 definition directories.
        final LinkedHashSet<HierarchicalResource> libSpoofax2DefinitionDirs = new LinkedHashSet<>(); // LinkedHashSet to remove duplicates while keeping insertion order.
        final LinkedHashSet<Supplier<ResourcePath>> libSpoofax2UnarchiveDirSuppliers = new LinkedHashSet<>();
        if(input.compileLanguageShared().includeLibSpoofax2Exports()) {
            final ClassLoaderResourceLocations locations = libSpoofax2ClassLoaderResources.definitionDirectory.getLocations();
            libSpoofax2DefinitionDirs.addAll(locations.directories);
            final ResourcePath libSpoofax2UnarchiveDirectory = input.compileLanguageShared().unarchiveDirectory().appendRelativePath("libspoofax2");
            for(JarFileWithPath jarFileWithPath : locations.jarFiles) {
                final ResourcePath jarFilePath = jarFileWithPath.file.getPath();
                final ResourcePath unarchiveDirectory = libSpoofax2UnarchiveDirectory.appendRelativePath(jarFilePath.getLeaf()); // JAR files always have leaves.
                libSpoofax2UnarchiveDirSuppliers.add(unarchiveFromJar
                    .createSupplier(new UnarchiveFromJar.Input(jarFilePath, unarchiveDirectory, false, false))
                    .map(new AppendPath(jarFileWithPath.path))
                );
            }
        }

        // Gather include directories.
        final LinkedHashSet<Supplier<ResourcePath>> includeDirSuppliers = new LinkedHashSet<>();
        includeDirSuppliers.add(new ValueSupplier<>(input.esvRootDirectory()));
        for(String export : LibSpoofax2Exports.getEsvExports()) {
            for(HierarchicalResource definitionDir : libSpoofax2DefinitionDirs) {
                final HierarchicalResource exportDirectory = definitionDir.appendAsRelativePath(export);
                if(exportDirectory.exists()) {
                    includeDirSuppliers.add(new ValueSupplier<>(exportDirectory.getPath()));
                }
            }
            for(Supplier<ResourcePath> unarchiveDirSupplier : libSpoofax2UnarchiveDirSuppliers) {
                includeDirSuppliers.add(unarchiveDirSupplier.map(new AppendPath(export)));
            }
        }

        // Check ESV source files.
        // TODO: this does not check ESV files in include directories.
        final @Nullable KeyedMessages messages = context.require(check.createTask(
            new EsvCheckMulti.Input(rootDirectory.getPath(), EsvUtil.createResourceWalker(), EsvUtil.createResourceMatcher())
        ));
        if(messages.containsError()) {
            return Result.ofErr(EsvCompileException.checkFail(messages));
        }

        // Compile ESV files to aterm format.
        final Result<IStrategoTerm, ?> result = context.require(compile, new EsvCompile.Input(
            parse.createAstSupplier(input.esvMainFile()),
            new ImportFunction(parse.createFunction(), includeDirSuppliers.stream().collect(Collectors.toCollection(ArrayList::new)), args.esvAdditionalAstSuppliers),
            ListView.of()
        ));
        if(result.isErr()) {
            return Result.ofErr(EsvCompileException.compileFail(result.unwrapErr()));
        }
        final IStrategoTerm atermFormat = result.unwrap();
        final WritableResource atermFormatFile = context.getWritableResource(input.esvAtermFormatOutputFile());
        atermFormatFile.writeString(atermFormat.toString());
        context.provide(atermFormatFile);

        return Result.ofOk(messages);
    }


    @ADT
    public abstract static class EsvCompileException extends Exception implements HasOptionalMessages {
        public interface Cases<R> {
            R mainFileFail(ResourceKey mainFile);

            R includeDirectoryFail(ResourcePath includeDirectory);

            R rootDirectoryFail(ResourcePath rootDirectory);

            R checkFail(KeyedMessages messages);

            R compileFail(Exception cause);
        }

        public static EsvCompileException mainFileFail(ResourceKey mainFile) {
            return EsvCompileExceptions.mainFileFail(mainFile);
        }

        public static EsvCompileException includeDirectoryFail(ResourcePath includeDirectory) {
            return EsvCompileExceptions.includeDirectoryFail(includeDirectory);
        }

        public static EsvCompileException rootDirectoryFail(ResourcePath rootDirectory) {
            return EsvCompileExceptions.rootDirectoryFail(rootDirectory);
        }

        public static EsvCompileException checkFail(KeyedMessages messages) {
            return EsvCompileExceptions.checkFail(messages);
        }

        public static EsvCompileException compileFail(Exception cause) {
            return withCause(EsvCompileExceptions.compileFail(cause), cause);
        }

        private static EsvCompileException withCause(EsvCompileException e, Exception cause) {
            e.initCause(cause);
            return e;
        }


        public abstract <R> R match(Cases<R> cases);

        public static EsvCompileExceptions.CasesMatchers.TotalMatcher_MainFileFail cases() {
            return EsvCompileExceptions.cases();
        }

        public EsvCompileExceptions.CaseOfMatchers.TotalMatcher_MainFileFail caseOf() {
            return EsvCompileExceptions.caseOf(this);
        }

        public Optional<KeyedMessages> getMessages() {
            return EsvCompileExceptions.getMessages(this);
        }


        @Override public String getMessage() {
            return caseOf()
                .mainFileFail((mainFile) -> "ESV main file '" + mainFile + "' does not exist or is not a file")
                .includeDirectoryFail((includeDirectory) -> "ESV include directory '" + includeDirectory + "' does not exist or is not a directory")
                .rootDirectoryFail((rootDirectory) -> "ESV root directory '" + rootDirectory + "' does not exist or is not a directory")
                .checkFail((messages) -> "Parsing or checking ESV source files failed")
                .compileFail((cause) -> "ESV compiler failed unexpectedly")
                ;
        }

        @Override public Throwable fillInStackTrace() {
            return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
        }

        @Override public Optional<KeyedMessages> getOptionalMessages() {
            return EsvCompileExceptions.getMessages(this);
        }


        @Override public abstract int hashCode();

        @Override public abstract boolean equals(@Nullable Object obj);
    }


    private static class AppendPath implements SerializableFunction<ResourcePath, ResourcePath> {
        private final String path;

        private AppendPath(String path) {
            this.path = path;
        }

        @Override public ResourcePath apply(ResourcePath dir) {
            return dir.appendAsRelativePath(path);
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final AppendPath that = (AppendPath)o;
            return path.equals(that.path);
        }

        @Override public int hashCode() {
            return path.hashCode();
        }

        @Override public String toString() {
            return "AppendPath{" +
                "path='" + path + '\'' +
                '}';
        }
    }

    private static class ImportFunction implements Function<String, Result<IStrategoTerm, ?>> {
        private final Function<Supplier<String>, Result<JSGLR1ParseOutput, JSGLR1ParseException>> parse;
        private final ArrayList<Supplier<ResourcePath>> includeDirSuppliers;
        private final ArrayList<Supplier<Result<IStrategoTerm, ?>>> additionalAstSuppliers;

        public ImportFunction(
            Function<Supplier<String>, Result<JSGLR1ParseOutput, JSGLR1ParseException>> parse,
            ArrayList<Supplier<ResourcePath>> includeDirSuppliers,
            ArrayList<Supplier<Result<IStrategoTerm, ?>>> additionalAstSuppliers
        ) {
            this.parse = parse;
            this.includeDirSuppliers = includeDirSuppliers;
            this.additionalAstSuppliers = additionalAstSuppliers;
        }

        @Override public Result<IStrategoTerm, ?> apply(ExecContext context, String importName) {
            final EsvFileFromImport esvFileFromImport = new EsvFileFromImport(importName);
            final ArrayList<Exception> suppressedExceptions = new ArrayList<>();
            for(Supplier<ResourcePath> includeDirSupplier : includeDirSuppliers) {
                final Supplier<ResourcePath> pathSupplier = includeDirSupplier.map(esvFileFromImport);
                try {
                    final ResourcePath path = context.require(pathSupplier);
                    final ReadableResource resource = context.require(path, ResourceStampers.<ReadableResource>exists());
                    if(!resource.exists()) continue;
                    return context.require(parse, pathSupplier.map(new ReadString())).map(new GetAst());
                } catch(IOException e) {
                    suppressedExceptions.add(e);
                } catch(UncheckedIOException e) {
                    suppressedExceptions.add(e.getCause());
                }
            }
            for(Supplier<Result<IStrategoTerm, ?>> astSupplier : additionalAstSuppliers) {
                final Result<IStrategoTerm, ?> result = context.require(astSupplier);
                if(result.isErr()) {
                    suppressedExceptions.add(result.unwrapErr());
                    continue;
                }
                final IStrategoTerm ast = result.unwrapUnchecked();
                final String moduleName = EsvUtil.getNameFromModuleTerm(ast);
                if(importName.equals(moduleName)) {
                    return result;
                }
            }
            final Exception exception = new Exception("Could not import '" + importName + "', no ESV module found with that name in any import directory nor in any additional supplied ASTs");
            suppressedExceptions.forEach(exception::addSuppressed);
            return Result.ofErr(exception);
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final ImportFunction that = (ImportFunction)o;
            return parse.equals(that.parse) && includeDirSuppliers.equals(that.includeDirSuppliers) && additionalAstSuppliers.equals(that.additionalAstSuppliers);
        }

        @Override public int hashCode() {
            return Objects.hash(parse, includeDirSuppliers, additionalAstSuppliers);
        }

        @Override public String toString() {
            return "ImportFunction{" +
                "parse=" + parse +
                ", includeDirSuppliers=" + includeDirSuppliers +
                ", additionalAstSuppliers=" + additionalAstSuppliers +
                '}';
        }
    }

    private static class EsvFileFromImport implements SerializableFunction<ResourcePath, ResourcePath> {
        private final String importName;

        public EsvFileFromImport(String importName) {
            this.importName = importName;
        }

        @Override public ResourcePath apply(ResourcePath includeDir) {
            return includeDir.appendRelativePath(importName).ensureLeafExtension("esv").getNormalized();
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final EsvFileFromImport that = (EsvFileFromImport)o;
            return importName.equals(that.importName);
        }

        @Override public int hashCode() {
            return importName.hashCode();
        }

        @Override public String toString() {
            return "EsvFileFromImport{" +
                "importName='" + importName + '\'' +
                '}';
        }
    }

    private static class ReadString implements Function<ResourcePath, String> {
        @Override public String apply(ExecContext ctx, ResourcePath p) {
            try {
                return ctx.require(p).readString();
            } catch(IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override public int hashCode() { return 0; }

        @Override
        public boolean equals(Object obj) { return this == obj || obj != null && this.getClass() == obj.getClass(); }

        @Override public String toString() { return "ReadString()"; }
    }

    private static class GetAst implements SerializableFunction<JSGLR1ParseOutput, IStrategoTerm> {
        @Override public IStrategoTerm apply(JSGLR1ParseOutput output) {
            return output.ast;
        }

        @Override public int hashCode() { return 0; }

        @Override
        public boolean equals(Object obj) { return this == obj || obj != null && this.getClass() == obj.getClass(); }

        @Override public String toString() { return "GetAst()"; }
    }
}
