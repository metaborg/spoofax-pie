package mb.spoofax.lwb.compiler.generator;

import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.Conversion;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@Value.Enclosing
public class LanguageProjectGenerator {
    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends LanguageProjectGeneratorData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        ResourcePath rootDirectory();

        String id();

        String name();

        String javaClassIdPrefix();

        List<String> fileExtensions();

        boolean multiFileAnalysis();


        @Value.Default default String ppName() {
            // TODO: convert to Stratego ID instead of Java ID.
            return Conversion.nameToJavaId(name().toLowerCase());
        }
    }

    private final TemplateWriter spoofaxcCfgTemplate;
    private final TemplateWriter mainEsvTemplate;
    private final TemplateWriter startSdf3Template;
    private final TemplateWriter mainStatixTemplate;
    private final TemplateWriter mainStrategoTemplate;
    private final TemplateWriter sptTemplate;
    private final ResourceService resourceService;

    @Inject public LanguageProjectGenerator(
        TemplateCompiler templateCompiler,
        ResourceService resourceService
    ) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.spoofaxcCfgTemplate = templateCompiler.getOrCompileToWriter("spoofaxc.cfg.mustache");
        this.mainEsvTemplate = templateCompiler.getOrCompileToWriter("main.esv.mustache");
        this.startSdf3Template = templateCompiler.getOrCompileToWriter("start.sdf3.mustache");
        this.mainStatixTemplate = templateCompiler.getOrCompileToWriter("main.stx.mustache");
        this.mainStrategoTemplate = templateCompiler.getOrCompileToWriter("main.str2.mustache");
        this.sptTemplate = templateCompiler.getOrCompileToWriter("test.spt.mustache");
        this.resourceService = resourceService;
    }

    public void generate(Input input) throws IOException {
        final HierarchicalResource rootDirectory = resourceService.getHierarchicalResource(input.rootDirectory());
        rootDirectory.ensureDirectoryExists();
        spoofaxcCfgTemplate.write(rootDirectory.appendRelativePath("spoofaxc.cfg"), input);
        mainEsvTemplate.write(rootDirectory.appendRelativePath("src/main.esv"), input);
        startSdf3Template.write(rootDirectory.appendRelativePath("src/start.sdf3"), input);
        mainStatixTemplate.write(rootDirectory.appendRelativePath("src/main.stx"), input);
        mainStrategoTemplate.write(rootDirectory.appendRelativePath("src/main.str2"), input);
        sptTemplate.write(rootDirectory.appendRelativePath("test/test.spt"), input);
    }
}
