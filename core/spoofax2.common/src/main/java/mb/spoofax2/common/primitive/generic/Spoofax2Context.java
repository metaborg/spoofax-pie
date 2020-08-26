package mb.spoofax2.common.primitive.generic;

import mb.resource.QualifiedResourceKeyString;
import mb.resource.hierarchical.ResourcePath;

public class Spoofax2Context {
    public final String languageGroupId;
    public final String languageId;
    public final String languageVersion;
    public final ResourcePath languagePath;

    public Spoofax2Context(
        String languageGroupId,
        String languageId,
        String languageVersion,
        ResourcePath languagePath
    ) {
        this.languageGroupId = languageGroupId;
        this.languageId = languageId;
        this.languageVersion = languageVersion;
        this.languagePath = languagePath;
    }
}
