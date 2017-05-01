package mb.pipe.run.pluto.util;

import java.io.Serializable;

import javax.annotation.Nullable;

import build.pluto.dependency.Origin;
import mb.pipe.run.core.model.IContext;

public abstract class AInput implements Serializable {
    private static final long serialVersionUID = 1L;

    public final IContext context;
    public final @Nullable Origin origin;


    public AInput(IContext context, @Nullable Origin origin) {
        this.context = context;
        this.origin = origin;
    }

    public AInput(IContext context) {
        this(context, null);
    }
}
