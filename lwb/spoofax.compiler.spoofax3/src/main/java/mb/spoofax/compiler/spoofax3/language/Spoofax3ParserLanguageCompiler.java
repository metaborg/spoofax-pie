package mb.spoofax.compiler.spoofax3.language;

import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.spoofax.task.Sdf3CheckMulti;
import mb.sdf3.spoofax.task.Sdf3CreateSpec;
import mb.sdf3.spoofax.task.Sdf3ParseTableToFile;
import mb.sdf3.spoofax.task.Sdf3Spec;
import mb.sdf3.spoofax.task.Sdf3SpecToParseTable;
import mb.sdf3.spoofax.task.util.Sdf3Util;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.language.ParserLanguageCompiler;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.metaborg.sdf2table.parsetable.ParseTableConfiguration;

import javax.inject.Inject;
import java.io.Serializable;

@Value.Enclosing
public class Spoofax3ParserLanguageCompiler implements TaskDef<Spoofax3ParserLanguageCompiler.Input, Result<KeyedMessages, ParserCompilerException>> {
    private final Sdf3CreateSpec sdf3CreateSpec;
    private final Sdf3CheckMulti sdf3CheckMulti;
    private final Sdf3SpecToParseTable sdf3SpecToParseTable;
    private final Sdf3ParseTableToFile sdf3ParseTableToFile;

    @Inject public Spoofax3ParserLanguageCompiler(
        Sdf3CreateSpec sdf3CreateSpec,
        Sdf3CheckMulti sdf3CheckMulti,
        Sdf3SpecToParseTable sdf3SpecToParseTable,
        Sdf3ParseTableToFile sdf3ParseTableToFile
    ) {
        this.sdf3CreateSpec = sdf3CreateSpec;
        this.sdf3CheckMulti = sdf3CheckMulti;
        this.sdf3SpecToParseTable = sdf3SpecToParseTable;
        this.sdf3ParseTableToFile = sdf3ParseTableToFile;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Result<KeyedMessages, ParserCompilerException> exec(ExecContext context, Input input) throws Exception {
        // Check main file and root directory.
        final HierarchicalResource mainFile = context.require(input.sdf3MainFile(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainFile.exists() || !mainFile.isFile()) {
            return Result.ofErr(ParserCompilerException.mainFileFail(input.sdf3MainFile()));
        }
        final HierarchicalResource rootDirectory = context.require(input.sdf3RootDirectory(), ResourceStampers.<HierarchicalResource>exists());
        if(!rootDirectory.exists() || !rootDirectory.isDirectory()) {
            return Result.ofErr(ParserCompilerException.rootDirectoryFail(input.sdf3RootDirectory()));
        }

        // Check SDF3 specification
        final @Nullable KeyedMessages messages = context.require(sdf3CheckMulti.createTask(new Sdf3CheckMulti.Input(input.sdf3RootDirectory(), Sdf3Util.createResourceWalker(), Sdf3Util.createResourceMatcher())));
        if(messages.containsError()) {
            return Result.ofErr(ParserCompilerException.checkFail(messages));
        }

        // Compile SDF3 to a parse table
        final Supplier<Sdf3Spec> sdf3SpecSupplier = sdf3CreateSpec.createSupplier(new Sdf3CreateSpec.Input(input.sdf3RootDirectory(), input.sdf3MainFile()));
        final Supplier<Result<ParseTable, ?>> sdf3ToParseTableSupplier = sdf3SpecToParseTable.createSupplier(new Sdf3SpecToParseTable.Args(sdf3SpecSupplier, input.sdf3ParseTableConfiguration(), false));
        final Result<None, ?> compileResult = context.require(sdf3ParseTableToFile, new Sdf3ParseTableToFile.Args(sdf3ToParseTableSupplier, input.sdf3ParseTableOutputFile()));
        if(compileResult.isErr()) {
            return Result.ofErr(ParserCompilerException.createParseTableFail(compileResult.getErr()));
        }

        return Result.ofOk(messages);

        // TODO: sdf3 to signatures, and pass that to origin task of the stratego compiler.
        // TODO: sdf3 to parenthesize, and pass that to origin task of the stratego compiler.
        // TODO: sdf3 to pretty-printer, and pass that to origin task of the stratego compiler.
        // TODO: sdf3 to completer, and pass that to origin task of the stratego compiler.
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends Spoofax3ParserLanguageCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        @Value.Default default ResourcePath sdf3RootDirectory() {
            return languageProject().project().srcMainDirectory().appendRelativePath("sdf3");
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
            return generatedResourcesDirectory() // Generated resources directory, so that Gradle includes the parse table in the JAR file.
                .appendRelativePath(languageProject().packagePath()) // Append package path to make location unique, enabling JAR files to be merged.
                .appendRelativePath(sdf3ParseTableRelativePath()) // Append the relative path to the parse table.
                ;
        }


        /// Automatically provided sub-inputs

        LanguageProject languageProject();

        ResourcePath generatedResourcesDirectory();


        default void syncTo(ParserLanguageCompiler.Input.Builder builder) {
            builder.parseTableRelativePath(sdf3ParseTableRelativePath());
        }
    }
}
