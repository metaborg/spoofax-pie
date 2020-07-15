package mb.statix.multilang;

import mb.resource.ResourceKey;

import java.io.Serializable;
import java.util.Objects;

public class FileKey implements Serializable {

    private final ResourceKey resourceKey;
    private final LanguageId languageId;

    public FileKey(ResourceKey resourceKey, LanguageId languageId) {
        this.resourceKey = resourceKey;
        this.languageId = languageId;
    }

    public ResourceKey getResourceKey() {
        return resourceKey;
    }

    public LanguageId getLanguageId() {
        return languageId;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        FileKey fileKey = (FileKey)o;
        return resourceKey.equals(fileKey.resourceKey) &&
            languageId.equals(fileKey.languageId);
    }

    @Override public int hashCode() {
        return Objects.hash(resourceKey, languageId);
    }

    @Override public String toString() {
        return "FileKey{" +
            "resourceKey=" + resourceKey +
            ", languageId=" + languageId +
            '}';
    }
}
