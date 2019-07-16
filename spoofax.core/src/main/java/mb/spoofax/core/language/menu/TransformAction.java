package mb.spoofax.core.language.menu;

import mb.spoofax.core.language.transform.TransformRequest;

public class TransformAction implements MenuItem {
    private final TransformRequest transformRequest;


    public TransformAction(TransformRequest transformRequest) {
        this.transformRequest = transformRequest;
    }


    @Override public String getDisplayName() {
        return transformRequest.transformDef.getDisplayName();
    }

    @Override public void accept(MenuItemVisitor visitor) {
        visitor.transformAction(transformRequest);
    }
}
