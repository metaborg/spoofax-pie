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
import mb.str.spoofax.task.StrategoCompileToJava;
import org.immutables.value.Value;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.metaborg.sdf2table.parsetable.ParseTableConfiguration;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Value.Enclosing
public class Spoofax3LanguageProjectCompiler implements TaskDef<Spoofax3LanguageProjectCompiler.Input, None> {
    private final Sdf3CreateSpec sdf3CreateSpec;
    private final Sdf3SpecToParseTable sdf3SpecToParseTable;
    private final Sdf3ParseTableToFile sdf3ParseTableToFile;
    private final StrategoCompileToJava strategoCompileToJava;


    @Inject public Spoofax3LanguageProjectCompiler(
        Sdf3CreateSpec sdf3CreateSpec,
        Sdf3SpecToParseTable sdf3SpecToParseTable,
        Sdf3ParseTableToFile sdf3ParseTableToFile,
        StrategoCompileToJava strategoCompileToJava
    ) {
        this.sdf3CreateSpec = sdf3CreateSpec;
        this.sdf3SpecToParseTable = sdf3SpecToParseTable;
        this.sdf3ParseTableToFile = sdf3ParseTableToFile;
        this.strategoCompileToJava = strategoCompileToJava;
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

        // TODO: error when Stratego root directory does not exist.
        // TODO: error when a Stratego include directory does not exist.
        // TODO: error when Stratego main file does not exist.

        final StrategoCompileToJava.Args strategoCompileInput = new StrategoCompileToJava.Args(
            input.strategoRootDirectory(),
            input.strategoMainFile(),
            new ArrayList<>(input.strategoIncludeDirs()),
            new ArrayList<>(input.strategoBuiltinLibs()),
            input.strategoCacheDir().orElse(null),
            input.strategoOutputDir(),
            input.strategoOutputJavaPackageId(),
            new ArrayList<>()
        );
        context.require(strategoCompileToJava, strategoCompileInput);

        return None.instance;
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends Spoofax3LanguageProjectCompilerData.Input.Builder {}

        static Builder builder() { return new Input.Builder(); }


        @Value.Default default ResourcePath sdf3RootDirectory() {
            return languageProject().project().baseDirectory().appendRelativePath("src/main/sdf3");
        }

        @Value.Default default ResourcePath sdf3MainFile() {
            return sdf3RootDirectory().appendRelativePath("main.sdf3");
        }

        @Value.Default default ParseTableConfiguration sdf3ParseTableConfiguration() {
            return new ParseTableConfiguration(false, false, true, false, false, false);
        }

        @Value.Default default ResourcePath sdf3ParseTableOutputFile() {
            return languageProject().project().sourceMainResourcesDirectory().appendRelativePath("sdf3.tbl");
            // TODO: sync to language project
        }


        @Value.Default default ResourcePath strategoRootDirectory() {
            return languageProject().project().baseDirectory().appendRelativePath("src/main/str");
        }

        @Value.Default default ResourcePath strategoMainFile() {
            return strategoRootDirectory().appendRelativePath("main.str");
        }

        @Value.Default default List<ResourcePath> strategoIncludeDirs() {
            final ArrayList<ResourcePath> strategoIncludeDirs = new ArrayList<>();
            strategoIncludeDirs.add(strategoRootDirectory());
            return strategoIncludeDirs;
        }

        @Value.Default default List<String> strategoBuiltinLibs() {
            final ArrayList<String> strategoBuiltinLibs = new ArrayList<>();
            strategoBuiltinLibs.add("stratego-lib");
            return strategoBuiltinLibs;
        }

        @Value.Default default Optional<ResourcePath> strategoCacheDir() {
            return Optional.of(languageProject().project().buildDirectory().appendRelativePath("stratego-cache"));
        }

        @Value.Default default ResourcePath strategoOutputDir() {
            return languageProject().project().genSourceSpoofaxJavaDirectory();
            // TODO: sync to language project
            // TODO: should this include the package path?
        }

        @Value.Default default String strategoOutputJavaPackageId() {
            return languageProject().packageId() + ".strategies";
            // TODO: sync to language project
        }


        /// Automatically provided sub-inputs

        LanguageProject languageProject();
    }
}
