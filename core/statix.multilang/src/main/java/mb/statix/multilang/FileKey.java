package mb.statix.multilang;

import mb.resource.ResourceKey;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Objects;

@Value.Immutable
public interface FileKey extends Serializable {

    ResourceKey resourceKey();

    LanguageId languageId();
}
