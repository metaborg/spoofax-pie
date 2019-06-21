package mb.spoofax.intellij.psi;

import com.intellij.lang.Language;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public final class SpoofaxElementTypeManager {
    private final static class ScopeType {
        final String scope;
        final Supplier<SpoofaxElementType> typeSupplier;

        private ScopeType(String scope, Supplier<SpoofaxElementType> typeSupplier) {
            this.scope = scope;
            this.typeSupplier = typeSupplier;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final ScopeType that = (ScopeType) o;
            if(!scope.equals(that.scope)) return false;
            return typeSupplier.equals(that.typeSupplier);
        }

        @Override public int hashCode() {
            int result = scope.hashCode();
            result = 31 * result + typeSupplier.hashCode();
            return result;
        }
    }
    private final List<ScopeType> scopes;

    private final SpoofaxElementType rootElementType;
    private final SpoofaxElementType defaultElementType;
    private final SpoofaxElementType namedElementType;


    @Inject public SpoofaxElementTypeManager(Language language) {
        this.rootElementType = new SpoofaxRootElementType(language);
        this.defaultElementType = new SpoofaxElementType(language);
        this.namedElementType = new SpoofaxNamedElementType(language);
        this.scopes = Collections.singletonList(
            scopeType("entity.name", () -> this.namedElementType)
        );
    }


    public SpoofaxElementType getRootElementType() {
        return rootElementType;
    }

    public SpoofaxElementType getDefaultElementType() {
        return defaultElementType;
    }

    public SpoofaxElementType getNamedElementType() {
        return namedElementType;
    }

    public SpoofaxElementType getElementType(SpoofaxTokenType type) {
        return this.scopes.stream()
            .filter(p -> type.getScope().startsWith(p.scope))
            .map(p -> p.typeSupplier.get())
            .findFirst()
            .orElse(defaultElementType);
    }

    private static ScopeType scopeType(String prefix, Supplier<SpoofaxElementType> type) {
        return new ScopeType(prefix, type);
    }
}
