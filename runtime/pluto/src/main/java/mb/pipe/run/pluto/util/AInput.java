package mb.pipe.run.pluto.util;

import java.io.Serializable;

import javax.annotation.Nullable;

import build.pluto.dependency.Origin;
import mb.pipe.run.core.model.Context;

public abstract class AInput implements Serializable {
    private static final long serialVersionUID = 1L;

    public final Context context;
    public final @Nullable Origin origin;


    public AInput(Context context, @Nullable Origin origin) {
        this.context = context;
        this.origin = origin;
    }

    public AInput(Context context) {
        this(context, null);
    }
}
