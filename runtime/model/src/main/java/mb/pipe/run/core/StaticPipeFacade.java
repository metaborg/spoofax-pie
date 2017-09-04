package mb.pipe.run.core;

public class StaticPipeFacade {
    private static PipeFacade facade;


    public static void init(PipeFacade facade) {
        StaticPipeFacade.facade = facade;
    }


    public static PipeFacade facade() {
        return facade;
    }
}
