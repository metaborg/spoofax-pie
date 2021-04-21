package mb.spoofax.lwb.dynamicloading;

import mb.common.util.SetView;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

@DynamicLoadingScope
public class DynamicLanguageRegistry implements AutoCloseable {
    private final Logger logger;

    private final HashMap<ResourcePath, DynamicLanguage> languagePerRootDirectory = new HashMap<>();
    private final HashMap<String, DynamicLanguage> languagePerId = new HashMap<>();
    private final HashMap<String, DynamicLanguage> languagePerFileExtension = new HashMap<>();
    private final Set<DynamicLanguageRegistryListener> listeners = new LinkedHashSet<>();


    @Inject public DynamicLanguageRegistry(LoggerFactory loggerFactory) {
        this.logger = loggerFactory.create(getClass());
    }

    @Override public void close() {
        final RuntimeException exception = new RuntimeException("Closing one or more dynamically loaded languages failed; resources may have been leaked");
        languagePerRootDirectory.forEach((rootDirectory, language) -> {
            try {
                language.close();
            } catch(IOException e) {
                exception.addSuppressed(e);
            }
        });
        if(exception.getSuppressed().length > 0) {
            throw exception;
        }
        languagePerRootDirectory.clear();
        languagePerFileExtension.clear();
    }


    public Iterable<DynamicLanguage> getLanguages() {
        return languagePerId.values();
    }

    public @Nullable DynamicLanguage getLanguageForRootDirectory(ResourcePath rootDirectory) {
        return languagePerRootDirectory.get(rootDirectory);
    }

    public @Nullable DynamicLanguage getLanguageForId(String id) {
        return languagePerId.get(id);
    }

    public @Nullable DynamicLanguage getLanguageForFileExtension(String fileExtension) {
        return languagePerFileExtension.get(fileExtension);
    }


    public void registerListener(DynamicLanguageRegistryListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(DynamicLanguageRegistryListener listener) {
        listeners.remove(listener);
    }


    public void reload(ResourcePath rootDirectory, DynamicLanguage language) {
        final @Nullable DynamicLanguage languageForId = languagePerId.get(language.getId());
        if(languageForId != null && !languageForId.getRootDirectory().equals(language.getRootDirectory())) {
            logger.error("Cannot register dynamically loaded language '{}' with ID '{}', a different language '{}' is already registered with that ID. Not loading language", language, language.getId(), languageForId);
            return;
        }

        final @Nullable DynamicLanguage previousLanguage = languagePerRootDirectory.put(rootDirectory, language);
        final Set<String> previousFileExtensions;
        if(previousLanguage != null) {
            previousFileExtensions = unregisterLanguage(previousLanguage);
        } else {
            previousFileExtensions = Collections.emptySet();
        }
        languagePerId.put(language.getId(), language);

        final Set<String> newFileExtensions = language.getFileExtensions().asCopy();
        final Set<String> removedFileExtensions = new LinkedHashSet<>(previousFileExtensions);
        removedFileExtensions.removeAll(newFileExtensions);
        final Set<String> addedFileExtensions = new LinkedHashSet<>(newFileExtensions);
        newFileExtensions.removeAll(previousFileExtensions);
        for(String extension : new LinkedHashSet<>(addedFileExtensions) /* Copy because we are removing elements during iteration */) {
            final @Nullable DynamicLanguage languageForExtension = languagePerFileExtension.get(extension);
            if(languageForExtension != null) {
                logger.warn("Cannot register dynamically loaded language '{}' with file extension '{}', language '{}' is already registered with that file extension. Skipping this extension", language, extension, languageForExtension);
                addedFileExtensions.remove(extension);
                continue;
            }
            languagePerFileExtension.put(extension, language);
        }

        if(previousLanguage != null && previousLanguage.getId().equals(language.getId())) {
            notifyReload(previousLanguage, language, SetView.of(removedFileExtensions), SetView.of(addedFileExtensions));
        } else {
            notifyLoad(language, SetView.of(addedFileExtensions));
        }
    }

    public void unload(ResourcePath rootDirectory) {
        final @Nullable DynamicLanguage language = languagePerRootDirectory.remove(rootDirectory);
        if(language != null) {
            final Set<String> removedFileExtensions = unregisterLanguage(language);
            notifyUnload(language, SetView.of(removedFileExtensions));
        }
    }


    private Set<String> unregisterLanguage(DynamicLanguage language) {
        final Set<String> removedFileExtensions = new LinkedHashSet<>();
        languagePerId.remove(language.getId());
        for(String extension : language.getFileExtensions()) {
            if(languagePerFileExtension.remove(extension, language)) {
                removedFileExtensions.add(extension);
            }
        }
        try {
            language.close();
        } catch(IOException e) {
            logger.error("Failed to close dynamically loaded language '{}'; resources may have been leaked", e, language);
        }
        return removedFileExtensions;
    }


    private void notifyLoad(DynamicLanguage language, SetView<String> addedFileExtensions) {
        listeners.forEach(l -> l.load(language, addedFileExtensions));
    }

    private void notifyReload(DynamicLanguage previousLanguage, DynamicLanguage language, SetView<String> removedFileExtensions, SetView<String> addedFileExtensions) {
        listeners.forEach(l -> l.reload(previousLanguage, language, removedFileExtensions, addedFileExtensions));
    }

    private void notifyUnload(DynamicLanguage language, SetView<String> removedFileExtensions) {
        listeners.forEach(l -> l.unload(language, removedFileExtensions));
    }
}
