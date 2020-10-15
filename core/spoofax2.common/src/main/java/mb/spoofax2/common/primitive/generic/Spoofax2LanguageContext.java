package mb.spoofax2.common.primitive.generic;

import mb.resource.hierarchical.ResourcePath;

import java.io.Serializable;
import java.util.Objects;

public class Spoofax2LanguageContext implements Serializable {
    public final String languageGroupId;
    public final String languageId;
    public final String languageVersion;
    public final ResourcePath languagePath;

    public Spoofax2LanguageContext(
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

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Spoofax2LanguageContext that = (Spoofax2LanguageContext)o;
        return languageGroupId.equals(that.languageGroupId) && languageId.equals(that.languageId) && languageVersion.equals(that.languageVersion) && languagePath.equals(that.languagePath);
    }

    @Override public int hashCode() {
        return Objects.hash(languageGroupId, languageId, languageVersion, languagePath);
    }

    @Override public String toString() {
        return "Spoofax2LanguageContext{" +
            "languageGroupId='" + languageGroupId + '\'' +
            ", languageId='" + languageId + '\'' +
            ", languageVersion='" + languageVersion + '\'' +
            ", languagePath=" + languagePath +
            '}';
    }
}
