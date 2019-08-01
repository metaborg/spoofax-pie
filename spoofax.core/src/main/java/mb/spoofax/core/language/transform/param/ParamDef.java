package mb.spoofax.core.language.transform.param;

import mb.common.util.CollectionView;

public class ParamDef {
    public final CollectionView<Param> params;

    public ParamDef(CollectionView<Param> params) {
        this.params = params;
    }
}
