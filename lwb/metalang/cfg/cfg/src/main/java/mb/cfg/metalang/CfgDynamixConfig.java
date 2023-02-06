package mb.cfg.metalang;

import mb.cfg.CompileMetaLanguageSourcesShared;
import mb.resource.hierarchical.ResourcePath;
import org.immutables.value.Value;

import java.io.Serializable;

/**
 * Configuration for Dynamix in the context of CFG.
 */
@Value.Immutable
public interface CfgDynamixConfig extends Serializable {
    class Builder extends ImmutableCfgDynamixConfig.Builder {}

    static Builder builder() {return new Builder();}

    @Value.Default default CfgDynamixSource source() {
        return CfgDynamixSource.files(CfgDynamixSource.Files.builder()
            .compileMetaLanguageSourcesShared(compileLanguageShared())
            .build()
        );
    }

    @Value.Default default ResourcePath generatedSourcesDirectory() {
        return compileLanguageShared().generatedSourcesDirectory().appendRelativePath("dynamix");
    }

    default ResourcePath outputSpecAtermDirectory() {
        return compileLanguageShared().generatedResourcesDirectory() // Generated resources directory, so that Gradle includes the aterm format file in the JAR file.
            .appendRelativePath(compileLanguageShared().languageProject().packagePath()) // Append package path to make location unique, enabling JAR files to be merged.
            ;
    }

    CompileMetaLanguageSourcesShared compileLanguageShared();
}
