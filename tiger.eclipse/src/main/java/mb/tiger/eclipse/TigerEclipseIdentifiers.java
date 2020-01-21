package mb.tiger.eclipse;

import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.eclipse.EclipseIdentifiers;

@LanguageScope
public class TigerEclipseIdentifiers implements EclipseIdentifiers {
    @Override public String getPlugin() {
        return TigerPlugin.pluginId;
    }

    @Override public String getContext() {
        return "tiger.eclipse.context";
    }

    @Override public String getDocumentProvider() {
        return "tiger.eclipse.documentprovider";
    }

    @Override public String getEditor() {
        return "tiger.eclipse.editor";
    }

    @Override public String getNature() {
        return TigerNature.id;
    }

    @Override public String getProjectBuilder() {
        return TigerProjectBuilder.id;
    }


    @Override public String getBaseMarker() {
        return "tiger.eclipse.marker";
    }

    @Override public String getInfoMarker() {
        return "tiger.eclipse.marker.info";
    }

    @Override public String getWarningMarker() {
        return "tiger.eclipse.marker.warning";
    }

    @Override public String getErrorMarker() {
        return "tiger.eclipse.marker.error";
    }


    @Override public String getAddNatureCommand() {
        return "tiger.eclipse.nature.add";
    }

    @Override public String getRemoveNatureCommand() {
        return "tiger.eclipse.nature.remove";
    }

    @Override public String getObserveCommand() {
        return "tiger.eclipse.observe";
    }

    @Override public String getUnobserveCommand() {
        return "tiger.eclipse.unobserve";
    }

    @Override public String getRunCommand() {
        return "tiger.eclipse.command";
    }


    @Override public String getResourceContextMenu() {
        return "tiger.eclipse.menu.resource.context";
    }

    @Override public String getEditorContextMenu() {
        return "tiger.eclipse.menu.editor.context";
    }

    @Override public String getMainMenu() {
        return "tiger.eclipse.menu.main";
    }

    @Override public String getMainMenuDynamic() {
        return "tiger.eclipse.menu.dynamic";
    }
}
