package mb.tiger.eclipse;

import mb.spoofax.eclipse.EclipseIdentifiers;

class TigerEclipseIdentifiers implements EclipseIdentifiers {
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
}
