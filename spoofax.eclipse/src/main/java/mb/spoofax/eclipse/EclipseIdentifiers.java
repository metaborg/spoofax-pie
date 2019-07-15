package mb.spoofax.eclipse;

public interface EclipseIdentifiers {
    String getNature();


    String getBuilder();


    String getAddNatureCommand();

    String getRemoveAddNatureCommand();

    String getObserveCommand();

    String getUnobserveCommand();


    String baseMarker();

    String infoMarker();

    String warningMarker();

    String errorMarker();
}
