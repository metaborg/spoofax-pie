package mb.statix.multilang.metadata;

import java.io.Serializable;
import java.util.Objects;

public class LanguageId implements Serializable {
    private final String languageId;

    public LanguageId(String languageId) {
        this.languageId = languageId;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        LanguageId that = (LanguageId)o;
        return languageId.equals(that.languageId);
    }

    @Override public int hashCode() {
        return Objects.hash(languageId);
    }

    @Override public String toString() {
        return languageId;
    }

    public String getId() {
        return languageId;
    }
}
