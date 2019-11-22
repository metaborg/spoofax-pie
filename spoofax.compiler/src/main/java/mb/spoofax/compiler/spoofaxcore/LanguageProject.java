package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.JavaDependency;
import mb.spoofax.compiler.util.JavaProject;
import mb.spoofax.compiler.util.ResourceWriter;
import mb.spoofax.compiler.util.TemplateCompiler;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static mb.spoofax.compiler.util.StringUtil.doubleQuote;

@Value.Enclosing
public class LanguageProject {
    private final ResourceService resourceService;
    private final Template settingsGradleTemplate;
    private final Template buildGradleTemplate;

    private LanguageProject(ResourceService resourceService, Template settingsGradleTemplate, Template buildGradleTemplate) {
        this.resourceService = resourceService;
        this.settingsGradleTemplate = settingsGradleTemplate;
        this.buildGradleTemplate = buildGradleTemplate;
    }

    public static LanguageProject fromClassLoaderResources(ResourceService resourceService) {
        final TemplateCompiler templateCompiler = new TemplateCompiler(LanguageProject.class);
        return new LanguageProject(
            resourceService,
            templateCompiler.compile("language_project/settings.gradle.kts.mustache"),
            templateCompiler.compile("language_project/build.gradle.kts.mustache")
        );
    }


    public Output compile(Input input, Charset charset) throws IOException {
        final Output output = Output.builder().withDefaultsBasedOnInput(input).build();

        final HierarchicalResource baseDirectory = resourceService.getHierarchicalResource(output.baseDirectory());
        baseDirectory.ensureDirectoryExists();

        final HierarchicalResource settingsGradleKtsFile = resourceService.getHierarchicalResource(output.settingsGradleKtsFile());
        try(final ResourceWriter writer = new ResourceWriter(settingsGradleKtsFile, charset)) {
            settingsGradleTemplate.execute(input, writer);
            writer.flush();
        }

        final HierarchicalResource buildGradleKtsFile = resourceService.getHierarchicalResource(output.buildGradleKtsFile());
        try(final ResourceWriter writer = new ResourceWriter(buildGradleKtsFile, charset)) {
            final HashMap<String, Object> map = new HashMap<>();

            final String languageDependencyCode = input.languageSpecificationDependency().caseOf()
                .project((projectPath) -> "createProjectDependency(\"" + projectPath + "\")")
                .module((coordinate) -> "createModuleDependency(\"" + coordinate.gradleNotation() + "\")")
                .files((filePaths) -> "createFilesDependency(" + filePaths.stream().map((s) -> "\"" + s + "\"").collect(Collectors.joining(", ")) + ")");
            map.put("languageDependencyCode", languageDependencyCode);

            final ArrayList<String> dependencyCodes = new ArrayList<>();
            final ArrayList<String> resourceCodes = new ArrayList<>();

            // Parser
            dependencyCodes.add(apiDependency(input.shared().jsglr1CommonDep()));
            resourceCodes.add(doubleQuote("target/metaborg/sdf.tbl"));

            // Styler
            if(input.enableStyler()) {
                dependencyCodes.add(apiDependency(input.shared().esvCommonDep()));
                resourceCodes.add(doubleQuote("target/metaborg/editor.esv.af"));
            }

            // Stratego
            if(input.enableStrategoTransformations()) {
                dependencyCodes.add(apiDependency(input.shared().strategoCommonDep()));
                dependencyCodes.add(apiDependency(input.shared().orgStrategoXTStrjDep()));
                dependencyCodes.add(implementationDependency(input.shared().strategoXTMinJarDep()));
                if(input.copyStrategoCTree()) {
                    resourceCodes.add(doubleQuote("target/metaborg/stratego.ctree"));
                }
            }

            // Constraint
            if(input.enableConstraintAnalysis()) {
                dependencyCodes.add(apiDependency(input.shared().constraintCommonDep()));
                // NaBL2 (required by Statix as well)
                if(input.enableNaBL2ConstraintGeneration() || input.enableStatixConstraintGeneration()) {
                    dependencyCodes.add(implementationDependency(input.shared().nabl2CommonDep()));
                }
                if(input.enableStatixConstraintGeneration()) {
                    dependencyCodes.add(implementationDependency(input.shared().statixCommonDep()));
                    resourceCodes.add(doubleQuote("src-gen/statix/statics.spec.aterm"));
                }
            }

            // Additional resources
            input.additionalCopyResources().forEach((resource) -> {
                resourceCodes.add(doubleQuote(resource));
            });

            map.put("dependencyCodes", dependencyCodes);
            map.put("resourceCodes", resourceCodes);

            buildGradleTemplate.execute(input, map, writer);
            writer.flush();
        }

        return output;
    }

    private static String apiDependency(JavaDependency dependency) {
        return "api(" + dependency.toGradleKotlinDependencyCode() + ")";
    }

    private static String implementationDependency(JavaDependency dependency) {
        return "implementation(" + dependency.toGradleKotlinDependencyCode() + ")";
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends LanguageProjectData.Input.Builder {}

        static Builder builder() {
            return new Builder();
        }


        Shared shared();


        @Value.Default default JavaProject project() {
            final Shared shared = shared();
            final String artifactId = shared.defaultArtifactId() + ".lang";
            return JavaProject.builder()
                .coordinate(shared.defaultGroupId(), artifactId, shared.defaultVersion())
                .packageId(shared.basePackageId())
                .baseDirectory(shared.baseDirectory().appendSegment(artifactId))
                .build();
        }


        JavaDependency languageSpecificationDependency();


        @Value.Default default boolean enableStyler() { return true; }

        boolean enableStrategoTransformations();

        boolean copyStrategoCTree();

        boolean copyStrategoClasses();

        boolean copyStrategoJavaStrategyClasses();

        boolean enableConstraintAnalysis();

        boolean enableNaBL2ConstraintGeneration();

        boolean enableStatixConstraintGeneration();


        List<String> additionalCopyResources();
    }

    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends LanguageProjectData.Output.Builder {
            public Builder withDefaultsBasedOnInput(Input input) {
                final ResourcePath baseDirectory = input.project().baseDirectory();
                return this
                    .baseDirectory(baseDirectory)
                    .settingsGradleKtsFile(baseDirectory.appendRelativePath("settings.gradle.kts"))
                    .buildGradleKtsFile(baseDirectory.appendRelativePath("build.gradle.kts"))
                    ;
            }
        }

        static Builder builder() {
            return new Builder();
        }


        ResourcePath baseDirectory();

        ResourcePath settingsGradleKtsFile();

        ResourcePath buildGradleKtsFile();
    }
}
