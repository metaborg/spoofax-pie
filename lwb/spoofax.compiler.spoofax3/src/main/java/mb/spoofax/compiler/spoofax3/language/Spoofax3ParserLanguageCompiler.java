package mb.spoofax.compiler.spoofax3.language;

import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.common.util.StreamIterable;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.None;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.AllResourceMatcher;
import mb.resource.hierarchical.match.FileResourceMatcher;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.sdf3.spoofax.task.Sdf3AnalyzeMulti;
import mb.sdf3.spoofax.task.Sdf3CheckMulti;
import mb.sdf3.spoofax.task.Sdf3CreateSpec;
import mb.sdf3.spoofax.task.Sdf3Desugar;
import mb.sdf3.spoofax.task.Sdf3Parse;
import mb.sdf3.spoofax.task.Sdf3ParseTableToFile;
import mb.sdf3.spoofax.task.Sdf3ParseTableToParenthesizer;
import mb.sdf3.spoofax.task.Sdf3Spec;
import mb.sdf3.spoofax.task.Sdf3SpecToParseTable;
import mb.sdf3.spoofax.task.Sdf3ToCompletionRuntime;
import mb.sdf3.spoofax.task.Sdf3ToPrettyPrinter;
import mb.sdf3.spoofax.task.Sdf3ToSignature;
import mb.sdf3.spoofax.task.util.Sdf3Util;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.language.ParserLanguageCompiler;
import mb.str.spoofax.task.StrategoPrettyPrint;
import mb.stratego.common.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.metaborg.sdf2table.parsetable.ParseTableConfiguration;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.stream.Stream;

import static mb.constraint.pie.ConstraintAnalyzeMultiTaskDef.SingleFileOutput;

@Value.Enclosing
public class Spoofax3ParserLanguageCompiler implements TaskDef<Spoofax3ParserLanguageCompiler.Input, Result<KeyedMessages, ParserCompilerException>> {
    private final Sdf3Parse parse;
    private final Function<Supplier<? extends Result<IStrategoTerm, ?>>, Result<IStrategoTerm, ?>> desugar;
    private final Sdf3AnalyzeMulti analyze;
    private final Sdf3CheckMulti check;
    private final Sdf3CreateSpec createSpec;
    private final Sdf3SpecToParseTable toParseTable;
    private final Sdf3ParseTableToFile parseTableToFile;
    private final StrategoPrettyPrint strategoPrettyPrint;
    private final Sdf3ToSignature toSignature;
    private final Sdf3ToPrettyPrinter toPrettyPrinter;
    private final Sdf3ParseTableToParenthesizer toParenthesizer;
    private final Sdf3ToCompletionRuntime toCompletionRuntime;

    @Inject public Spoofax3ParserLanguageCompiler(
        Sdf3Parse parse,
        Sdf3Desugar desugar,
        Sdf3AnalyzeMulti analyze,
        Sdf3CreateSpec createSpec,
        Sdf3CheckMulti check,
        Sdf3SpecToParseTable toParseTable,
        Sdf3ParseTableToFile parseTableToFile,
        StrategoPrettyPrint strategoPrettyPrint,
        Sdf3ToSignature toSignature,
        Sdf3ToPrettyPrinter toPrettyPrinter,
        Sdf3ParseTableToParenthesizer toParenthesizer,
        Sdf3ToCompletionRuntime toCompletionRuntime
    ) {
        this.parse = parse;
        this.desugar = desugar.createFunction();
        this.analyze = analyze;
        this.check = check;
        this.createSpec = createSpec;
        this.toParseTable = toParseTable;
        this.parseTableToFile = parseTableToFile;
        this.strategoPrettyPrint = strategoPrettyPrint;
        this.toSignature = toSignature;
        this.toPrettyPrinter = toPrettyPrinter;
        this.toParenthesizer = toParenthesizer;
        this.toCompletionRuntime = toCompletionRuntime;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<KeyedMessages, ParserCompilerException> exec(ExecContext context, Input input) throws Exception {
        // Check main file and root directory.
        final HierarchicalResource mainFile = context.require(input.sdf3MainFile(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainFile.exists() || !mainFile.isFile()) {
            return Result.ofErr(ParserCompilerException.mainFileFail(mainFile.getPath()));
        }
        final HierarchicalResource rootDirectory = context.require(input.sdf3RootDirectory(), ResourceStampers.<HierarchicalResource>exists());
        if(!rootDirectory.exists() || !rootDirectory.isDirectory()) {
            return Result.ofErr(ParserCompilerException.rootDirectoryFail(rootDirectory.getPath()));
        }

        // Check SDF3 source files.
        final ResourceWalker resourceWalker = Sdf3Util.createResourceWalker();
        final ResourceMatcher resourceMatcher = new AllResourceMatcher(Sdf3Util.createResourceMatcher(), new FileResourceMatcher());
        final @Nullable KeyedMessages messages = context.require(check.createTask(
            new Sdf3CheckMulti.Input(rootDirectory.getPath(), resourceWalker, resourceMatcher)
        ));
        if(messages.containsError()) {
            return Result.ofErr(ParserCompilerException.checkFail(messages));
        }

        // Compile SDF3 sources to a parse table.
        final Supplier<Sdf3Spec> specSupplier = createSpec.createSupplier(new Sdf3CreateSpec.Input(input.sdf3RootDirectory(), input.sdf3MainFile()));
        final Supplier<Result<ParseTable, ?>> parseTableSupplier = toParseTable.createSupplier(new Sdf3SpecToParseTable.Args(specSupplier, input.sdf3ParseTableConfiguration(), false));
        final Result<None, ? extends Exception> compileResult = context.require(parseTableToFile, new Sdf3ParseTableToFile.Args(parseTableSupplier, input.sdf3ParseTableOutputFile()));
        if(compileResult.isErr()) {
            return Result.ofErr(ParserCompilerException.parseTableCompilerFail(compileResult.getErr()));
        }

        final Sdf3AnalyzeMulti.Input analyzeInput = new Sdf3AnalyzeMulti.Input(rootDirectory.getPath(), resourceWalker, resourceMatcher, parse.createRecoverableAstFunction());
        try(final Stream<? extends HierarchicalResource> stream = rootDirectory.walk(resourceWalker, resourceMatcher)) {
            for(HierarchicalResource file : new StreamIterable<>(stream)) {
                final Supplier<Result<SingleFileOutput, ?>> singleFileAnalysisOutputSupplier = analyze.createSingleFileOutputSupplier(analyzeInput, file.getPath());
                try {
                    toSignature(context, input, singleFileAnalysisOutputSupplier);
                } catch(Exception e) {
                    return Result.ofErr(ParserCompilerException.signatureGeneratorFail(e));
                }

                final Supplier<Result<IStrategoTerm, ?>> astSupplier = desugar.createSupplier(parse.createAstSupplier(file.getPath()));
                try {
                    toPrettyPrinter(context, input, astSupplier);
                } catch(Exception e) {
                    return Result.ofErr(ParserCompilerException.prettyPrinterGeneratorFail(e));
                }
                try {
                    toCompletionRuntime(context, input, astSupplier);
                } catch(Exception e) {
                    return Result.ofErr(ParserCompilerException.completionRuntimeGeneratorFail(e));
                }
            }
        }

        try {
            toParenthesizer(context, input, parseTableSupplier);
        } catch(Exception e) {
            return Result.ofErr(ParserCompilerException.parenthesizerGeneratorFail(e));
        }

        return Result.ofOk(messages);
    }

    private void toSignature(ExecContext context, Input input, Supplier<Result<SingleFileOutput, ?>> singleFileAnalysisOutputSupplier) throws Exception {
        final Supplier<Result<IStrategoTerm, ?>> supplier = toSignature.createSupplier(singleFileAnalysisOutputSupplier);
        writePrettyPrintedStrategoFile(context, input, supplier);
    }

    private void toPrettyPrinter(ExecContext context, Input input, Supplier<Result<IStrategoTerm, ?>> astSupplier) throws Exception {
        final Supplier<Result<IStrategoTerm, ?>> supplier = toPrettyPrinter.createSupplier(astSupplier);
        writePrettyPrintedStrategoFile(context, input, supplier);
    }

    private void toParenthesizer(ExecContext context, Input input, Supplier<Result<ParseTable, ?>> parseTableSupplier) throws Exception {
        final Supplier<Result<IStrategoTerm, ?>> supplier = toParenthesizer.createSupplier(new Sdf3ParseTableToParenthesizer.Args(parseTableSupplier, "main"));
        writePrettyPrintedStrategoFile(context, input, supplier);
    }

    private void toCompletionRuntime(ExecContext context, Input input, Supplier<Result<IStrategoTerm, ?>> astSupplier) throws Exception {
        final Supplier<Result<IStrategoTerm, ?>> supplier = toCompletionRuntime.createSupplier(astSupplier);
        writePrettyPrintedStrategoFile(context, input, supplier);
    }

    private void writePrettyPrintedStrategoFile(ExecContext context, Input input, Supplier<Result<IStrategoTerm, ?>> supplier) throws Exception {
        final Result<IStrategoTerm, ? extends Exception> result = context.require(supplier);
        final IStrategoTerm ast = result.unwrap();
        final String moduleName = TermUtils.toJavaStringAt(ast, 0);

        final Result<IStrategoTerm, ? extends Exception> prettyPrintedResult = context.require(strategoPrettyPrint, supplier);
        final IStrategoTerm prettyPrintedTerm = prettyPrintedResult.unwrap();
        final String prettyPrinted = TermUtils.toJavaString(prettyPrintedTerm);

        final HierarchicalResource file = context.getHierarchicalResource(input.spoofax3LanguageProject().generatedStrategoSourcesDirectory().appendRelativePath(moduleName).appendToLeaf(".str"));
        file.ensureFileExists();
        file.writeString(prettyPrinted);
        context.provide(file);
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends Spoofax3ParserLanguageCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        @Value.Default default ResourcePath sdf3RootDirectory() {
            return spoofax3LanguageProject().languageProject().project().srcMainDirectory().appendRelativePath("sdf3");
        }

        @Value.Default default ResourcePath sdf3MainFile() {
            return sdf3RootDirectory().appendRelativePath("start.sdf3");
        }

        @Value.Default default ParseTableConfiguration sdf3ParseTableConfiguration() {
            return new ParseTableConfiguration(
                false,
                false,
                true,
                false,
                false,
                false
            );
        }

        @Value.Default default String sdf3ParseTableRelativePath() {
            return "sdf.tbl";
        }

        default ResourcePath sdf3ParseTableOutputFile() {
            return spoofax3LanguageProject().generatedResourcesDirectory() // Generated resources directory, so that Gradle includes the parse table in the JAR file.
                .appendRelativePath(spoofax3LanguageProject().languageProject().packagePath()) // Append package path to make location unique, enabling JAR files to be merged.
                .appendRelativePath(sdf3ParseTableRelativePath()) // Append the relative path to the parse table.
                ;
        }


        /// Automatically provided sub-inputs

        Spoofax3LanguageProject spoofax3LanguageProject();


        default void syncTo(ParserLanguageCompiler.Input.Builder builder) {
            builder.parseTableRelativePath(sdf3ParseTableRelativePath());
        }

        default void syncTo(Spoofax3StrategoRuntimeLanguageCompiler.Input.Builder builder) {
            builder.addStrategoIncludeDirs(spoofax3LanguageProject().generatedStrategoSourcesDirectory());
        }
    }
}
