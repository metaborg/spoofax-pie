package mb.sdf3_ext_statix.task;

import mb.sdf3_ext_statix.Sdf3ExtStatixScope;
import mb.stratego.pie.AstStrategoTransformTaskDef;

import javax.inject.Inject;

@Sdf3ExtStatixScope
public class Sdf3ExtStatixGenerateStatix extends AstStrategoTransformTaskDef {
    @Inject public Sdf3ExtStatixGenerateStatix(Sdf3ExtStatixGetStrategoRuntimeProvider getStrategoRuntimeProvider) {
        super(getStrategoRuntimeProvider, "geninj-generate-statix");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
