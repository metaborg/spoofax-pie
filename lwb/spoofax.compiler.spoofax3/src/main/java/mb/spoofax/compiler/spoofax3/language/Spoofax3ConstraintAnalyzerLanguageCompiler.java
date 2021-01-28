package mb.spoofax.compiler.spoofax3.language;

import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.common.util.StreamIterable;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.AllResourceMatcher;
import mb.resource.hierarchical.match.FileResourceMatcher;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.spoofax.compiler.language.ConstraintAnalyzerLanguageCompiler;
import mb.statix.task.StatixCheckMulti;
import mb.statix.task.StatixCompile;
import mb.statix.util.StatixUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

@Value.Enclosing
public class Spoofax3ConstraintAnalyzerLanguageCompiler implements TaskDef<Spoofax3ConstraintAnalyzerLanguageCompiler.Input, Result<KeyedMessages, ConstraintAnalyzerCompilerException>> {
    private final StatixCheckMulti check;
    private final StatixCompile compile;

    @Inject public Spoofax3ConstraintAnalyzerLanguageCompiler(
        StatixCheckMulti check,
        StatixCompile compile
    ) {
        this.check = check;
        this.compile = compile;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<KeyedMessages, ConstraintAnalyzerCompilerException> exec(ExecContext context, Input input) throws Exception {
        // Check main file, root directory, and include directories.
        final HierarchicalResource mainFile = context.require(input.statixMainFile(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainFile.exists() || !mainFile.isFile()) {
            return Result.ofErr(ConstraintAnalyzerCompilerException.mainFileFail(mainFile.getPath()));
        }
        final HierarchicalResource rootDirectory = context.require(input.statixRootDirectory(), ResourceStampers.<HierarchicalResource>exists());
        if(!rootDirectory.exists() || !rootDirectory.isDirectory()) {
            return Result.ofErr(ConstraintAnalyzerCompilerException.rootDirectoryFail(rootDirectory.getPath()));
        }
        for(ResourcePath includeDirectoryPath : input.statixIncludeDirs()) {
            final HierarchicalResource includeDirectory = context.require(includeDirectoryPath, ResourceStampers.<HierarchicalResource>exists());
            if(!includeDirectory.exists() || !includeDirectory.isDirectory()) {
                return Result.ofErr(ConstraintAnalyzerCompilerException.includeDirectoryFail(includeDirectoryPath));
            }
        }

        // Check Statix source files.
        final ResourceWalker resourceWalker = StatixUtil.createResourceWalker();
        final ResourceMatcher resourceMatcher = new AllResourceMatcher(StatixUtil.createResourceMatcher(), new FileResourceMatcher());
        // TODO: this does not check Statix files in include directories.
        final @Nullable KeyedMessages messages = context.require(check.createTask(
            new StatixCheckMulti.Input(rootDirectory.getPath(), resourceWalker, resourceMatcher)
        ));
        if(messages.containsError()) {
            return Result.ofErr(ConstraintAnalyzerCompilerException.checkFail(messages));
        }

        final HierarchicalResource outputDirectory = context.getHierarchicalResource(input.statixOutputDirectory()).ensureDirectoryExists();
        try(final Stream<? extends HierarchicalResource> stream = rootDirectory.walk(resourceWalker, resourceMatcher)) {
            for(HierarchicalResource inputFile : new StreamIterable<>(stream)) {
                final Result<StatixCompile.Output, ?> result = context.require(compile, new StatixCompile.Input(rootDirectory.getPath(), inputFile.getPath()));
                if(result.isErr()) {
                    return Result.ofErr(ConstraintAnalyzerCompilerException.compilerFail(result.unwrapErr()));
                }
                final StatixCompile.Output output = result.unwrapUnchecked();
                final HierarchicalResource outputFile = outputDirectory.appendAsRelativePath(output.relativeOutputPath).ensureFileExists();
                outputFile.writeString(output.spec.toString());
                context.provide(outputFile);
            }
        }

        return Result.ofOk(messages);
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends Spoofax3ConstraintAnalyzerLanguageCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        @Value.Default default ResourcePath statixRootDirectory() {
            return spoofax3LanguageProject().languageProject().project().srcMainDirectory().appendRelativePath("statix");
        }

        @Value.Default default ResourcePath statixMainFile() {
            return statixRootDirectory().appendRelativePath("main.stx");
        }

        List<ResourcePath> statixIncludeDirs();


        default ResourcePath statixOutputDirectory() {
            return spoofax3LanguageProject().generatedResourcesDirectory() // Generated resources directory, so that Gradle includes the aterm format file in the JAR file.
                .appendRelativePath(spoofax3LanguageProject().languageProject().packagePath()) // Append package path to make location unique, enabling JAR files to be merged.
                ;
        }


        /// Automatically provided sub-inputs

        Spoofax3LanguageProject spoofax3LanguageProject();


        default void syncTo(ConstraintAnalyzerLanguageCompiler.Input.Builder builder) {
            builder.enableNaBL2(false);
            builder.enableStatix(true);
        }
    }
}
