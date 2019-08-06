package mb.tiger.eclipse;

import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.eclipse.EclipseIdentifiers;

@LanguageScope
public class TigerEclipseIdentifiers implements EclipseIdentifiers {
    @Override public String getContext() {
        return "tiger.eclipse.context";
    }

    @Override public String getNature() {
        return TigerProjectNature.id;
    }

    @Override public String getBuilder() {
        return TigerProjectBuilder.id;
    }


    @Override public String getAddNatureCommand() {
        return "tiger.eclipse.nature.add";
    }

    @Override public String getRemoveAddNatureCommand() {
        return "tiger.eclipse.nature.remove";
    }

    @Override public String getObserveCommand() {
        return "tiger.eclipse.observe";
    }

    @Override public String getUnobserveCommand() {
        return "tiger.eclipse.unobserve";
    }

    @Override public String getTransformCommand() {
        return "tiger.eclipse.command";
    }


    @Override public String baseMarker() {
        return "tiger.eclipse.marker";
    }

    @Override public String infoMarker() {
        return "tiger.eclipse.marker.info";
    }

    @Override public String warningMarker() {
        return "tiger.eclipse.marker.warning";
    }

    @Override public String errorMarker() {
        return "tiger.eclipse.marker.error";
    }
}
