package mb.spoofax.lwb.eclipse.dynamicloading;


import mb.cfg.CompileLanguageInput;
import mb.pie.dagger.PieComponent;
import mb.resource.dagger.ResourceRegistriesProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.menu.MenuShared;
import mb.spoofax.lwb.dynamicloading.DynamicLanguage;
import org.eclipse.core.commands.AbstractHandler;

import java.net.URLClassLoader;

public class EclipseDynamicLanguage extends DynamicLanguage {
    private final MenuShared resourceContextMenu;
    private final MenuShared editorContextMenu;
    private final MenuShared mainMenu;
    private final AbstractHandler runCommandHandler;

    public EclipseDynamicLanguage(
        ResourcePath rootDirectory,
        CompileLanguageInput compileInput,
        URLClassLoader classLoader,
        ResourceRegistriesProvider resourceRegistriesProvider,
        ResourceServiceComponent resourceServiceComponent,
        EclipseLanguageComponent languageComponent,
        PieComponent pieComponent,

        MenuShared resourceContextMenu,
        MenuShared editorContextMenu,
        MenuShared mainMenu,
        AbstractHandler runCommandHandler
    ) {
        super(rootDirectory, compileInput, classLoader, resourceRegistriesProvider, resourceServiceComponent, languageComponent, pieComponent);

        this.resourceContextMenu = resourceContextMenu;
        this.editorContextMenu = editorContextMenu;
        this.mainMenu = mainMenu;
        this.runCommandHandler = runCommandHandler;
    }

    @Override public EclipseLanguageComponent getLanguageComponent() {
        return (EclipseLanguageComponent)super.getLanguageComponent();
    }


    public MenuShared getResourceContextMenu() {
        if(closed)
            throw new IllegalStateException("Cannot get resource context menu, dynamically loaded language has been closed");
        return resourceContextMenu;
    }

    public MenuShared getEditorContextMenu() {
        if(closed)
            throw new IllegalStateException("Cannot get editor context menu, dynamically loaded language has been closed");
        return editorContextMenu;
    }

    public MenuShared getMainMenu() {
        if(closed)
            throw new IllegalStateException("Cannot get main menu, dynamically loaded language has been closed");
        return mainMenu;
    }

    public AbstractHandler getRunCommandHandler() {
        return runCommandHandler;
    }
}
