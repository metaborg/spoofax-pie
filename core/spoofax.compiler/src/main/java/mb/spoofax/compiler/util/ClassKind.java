package mb.spoofax.compiler.util;

import java.io.Serializable;

public enum ClassKind implements Serializable {
    /**
     * Generated class.
     */
    Generated,
    /**
     * Manually implemented class.
     */
    Manual;

    public boolean isGenerating() {
        return this == Generated;
    }

    public boolean isManual() {
        return this == Manual;
    }

}
