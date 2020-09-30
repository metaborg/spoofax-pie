package mb.spoofax.compiler.adapter.data;

import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
public interface Export extends Serializable {
    class Builder extends ImmutableExport.Builder {}

    static Builder builder() {
        return new Builder();
    }

    static ImmutableExport of(String languageId, String relativePath) {
        return ImmutableExport.of(languageId, relativePath);
    }


    @Value.Parameter String languageId();

    @Value.Parameter String relativePath();
}
