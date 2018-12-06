package mb.spoofax.legacy;

import mb.fs.java.JavaFSNode;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.*;
import org.metaborg.spoofax.core.Spoofax;

public class LanguageLoader {
    public static ILanguageComponent loadLanguage(JavaFSNode source) throws MetaborgException {
        final Spoofax spoofax = StaticSpoofaxCoreFacade.spoofax();
        final FileObject resource = spoofax.resourceService.resolve(source.getURI());
        final ILanguageComponentFactory languageComponentFactory = spoofax.languageComponentFactory;
        final IComponentCreationConfigRequest request;
        if(source.isFile()) {
            request = languageComponentFactory.requestFromArchive(resource);
        } else {
            request = languageComponentFactory.requestFromDirectory(resource);
        }
        final ComponentCreationConfig config = languageComponentFactory.createConfig(request);
        final ILanguageComponent component = spoofax.languageService.add(config);
        return component;
    }
}
