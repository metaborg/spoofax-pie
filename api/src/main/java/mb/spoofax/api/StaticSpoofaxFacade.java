package mb.spoofax.api;

public class StaticSpoofaxFacade {
    private static SpoofaxFacade facade;


    public static void init(SpoofaxFacade facade) {
        StaticSpoofaxFacade.facade = facade;
    }


    public static SpoofaxFacade facade() {
        return facade;
    }
}
