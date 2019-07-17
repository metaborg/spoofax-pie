package mb.spoofax.core.language.menu;

import mb.spoofax.core.language.transform.TransformRequest;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TransformAction implements MenuItem {
    private final TransformRequest transformRequest;
    private final @Nullable String displayName;


    public TransformAction(TransformRequest transformRequest, @Nullable String displayName) {
        this.transformRequest = transformRequest;
        this.displayName = displayName;
    }

    public TransformAction(TransformRequest transformRequest) {
        this(transformRequest, null);
    }


    public TransformRequest getTransformRequest() {
        return transformRequest;
    }

    @Override public String getDisplayName() {
        if(displayName != null) {
            return displayName;
        } else {
            return transformRequest.transformDef.getDisplayName();
        }
    }

    @Override public void accept(MenuItemVisitor visitor) {
        visitor.transformAction(getDisplayName(), transformRequest);
    }
}
