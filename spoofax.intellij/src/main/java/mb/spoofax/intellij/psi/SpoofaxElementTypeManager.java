package mb.spoofax.intellij.psi;

import com.intellij.lang.Language;
import javafx.util.Pair;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;


public final class SpoofaxElementTypeManager {
    private final List<Pair<String, Supplier<SpoofaxElementType>>> scopes;

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
            .filter(p -> type.getScope().startsWith(p.getKey()))
            .map(p -> p.getValue().get())
            .findFirst()
            .orElse(defaultElementType);
    }

    private static Pair<String, Supplier<SpoofaxElementType>> scopeType(String prefix, Supplier<SpoofaxElementType> type) {
        return new Pair<>(prefix, type);
    }
}
