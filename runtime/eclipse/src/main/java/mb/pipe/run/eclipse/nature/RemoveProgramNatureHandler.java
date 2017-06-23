package mb.pipe.run.eclipse.nature;

public class RemoveProgramNatureHandler extends RemoveNatureHandler {
    @Override protected String natureId() {
        return PipeProgramNature.id;
    }
}
