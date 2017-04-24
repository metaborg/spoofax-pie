package mb.pipe.run.pluto.util;

import java.io.File;
import java.io.Serializable;

import javax.annotation.Nullable;

import build.pluto.dependency.Origin;

public abstract class AInput implements Serializable {
    private static final long serialVersionUID = 1L;

    public final File depDir;
    public final @Nullable Origin origin;


    public AInput(File depDir, @Nullable Origin origin) {
        this.depDir = depDir;
        this.origin = origin;
    }

    public AInput(File depDir) {
        this(depDir, null);
    }
}
