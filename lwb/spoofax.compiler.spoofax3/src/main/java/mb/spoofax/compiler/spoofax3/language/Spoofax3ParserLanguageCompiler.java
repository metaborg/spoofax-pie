package mb.spoofax.compiler.spoofax3.language;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.spoofax.task.Sdf3CreateSpec;
import mb.sdf3.spoofax.task.Sdf3ParseTableToFile;
import mb.sdf3.spoofax.task.Sdf3Spec;
import mb.sdf3.spoofax.task.Sdf3SpecToParseTable;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.language.ParserLanguageCompiler;
import org.immutables.value.Value;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.metaborg.sdf2table.parsetable.ParseTableConfiguration;

import javax.inject.Inject;
import java.io.Serializable;

@Value.Enclosing
public class Spoofax3ParserLanguageCompiler implements TaskDef<Spoofax3ParserLanguageCompiler.Input, None> {
    private final Sdf3CreateSpec sdf3CreateSpec;
    private final Sdf3SpecToParseTable sdf3SpecToParseTable;
    private final Sdf3ParseTableToFile sdf3ParseTableToFile;

    @Inject public Spoofax3ParserLanguageCompiler(
        Sdf3CreateSpec sdf3CreateSpec,
        Sdf3SpecToParseTable sdf3SpecToParseTable,
        Sdf3ParseTableToFile sdf3ParseTableToFile
    ) {
        this.sdf3CreateSpec = sdf3CreateSpec;
        this.sdf3SpecToParseTable = sdf3SpecToParseTable;
        this.sdf3ParseTableToFile = sdf3ParseTableToFile;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public None exec(ExecContext context, Input input) throws Exception {
        // TODO: error when SDF3 root directory does not exist.
        // TODO: error when SDF3 main file does not exist.

        final Supplier<Sdf3Spec> sdf3SpecSupplier = sdf3CreateSpec.createSupplier(new Sdf3CreateSpec.Input(input.sdf3RootDirectory(), input.sdf3MainFile()));
        final Supplier<Result<ParseTable, ?>> sdf3ToParseTableSupplier = sdf3SpecToParseTable.createSupplier(new Sdf3SpecToParseTable.Args(sdf3SpecSupplier, input.sdf3ParseTableConfiguration(), false));
        context.require(sdf3ParseTableToFile, new Sdf3ParseTableToFile.Args(sdf3ToParseTableSupplier, input.sdf3ParseTableOutputFile()));

        // TODO: sdf3 to signatures, and pass that to origin task of the stratego compiler.
        // TODO: sdf3 to parenthesize, and pass that to origin task of the stratego compiler.
        // TODO: sdf3 to pretty-printer, and pass that to origin task of the stratego compiler.
        // TODO: sdf3 to completer, and pass that to origin task of the stratego compiler.

        return None.instance;
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends Spoofax3ParserLanguageCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        @Value.Default default ResourcePath sdf3RootDirectory() {
            return languageProject().project().baseDirectory().appendRelativePath("src/main/sdf3");
        }

        @Value.Default default ResourcePath sdf3MainFile() {
            return sdf3RootDirectory().appendRelativePath("main.sdf3");
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
            return languageProject().project()
                .genSourceSpoofaxResourcesDirectory() // Generated resources directory, so that Gradle includes the parse table in the JAR file.
                .appendRelativePath(languageProject().packagePath()) // Append package path to make location unique, enabling JAR files to be merged.
                .appendRelativePath(sdf3ParseTableRelativePath()); // Append the relative path to the parse table.
        }


        /// Automatically provided sub-inputs

        LanguageProject languageProject();


        default void syncTo(ParserLanguageCompiler.Input.Builder builder) {
            builder.parseTableRelativePath(sdf3ParseTableRelativePath());
        }
    }
}
