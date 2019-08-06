package mb.spoofax.core.language.command.arg;

import mb.common.util.CollectionView;

public class ParamDef {
    public final CollectionView<Param> params;

    public ParamDef(CollectionView<Param> params) {
        this.params = params;
    }
}
