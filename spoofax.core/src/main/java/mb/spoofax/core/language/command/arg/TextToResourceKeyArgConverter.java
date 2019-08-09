package mb.spoofax.core.language.command.arg;

import mb.resource.ResourceKey;
import mb.resource.text.TextResourceRegistry;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TextToResourceKeyArgConverter implements ArgConverter<ResourceKey> {
    private final TextResourceRegistry textResourceRegistry;

    @Inject public TextToResourceKeyArgConverter(TextResourceRegistry textResourceRegistry) {
        this.textResourceRegistry = textResourceRegistry;
    }

    @Override public ResourceKey convert(String argStr) throws Exception {
        return textResourceRegistry.createResourceWithRandomUUID(argStr).key;
    }

    @Override public Class<ResourceKey> getOutputClass() {
        return ResourceKey.class;
    }
}
