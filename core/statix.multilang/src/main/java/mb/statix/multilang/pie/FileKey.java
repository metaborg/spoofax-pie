package mb.statix.multilang.pie;

import mb.resource.ResourceKey;
import mb.statix.multilang.metadata.LanguageId;
import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
public interface FileKey extends Serializable {

    ResourceKey resourceKey();

    LanguageId languageId();
}
