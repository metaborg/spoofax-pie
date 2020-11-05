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
        switch(this) {
            case Generated:
            case Extended:
                return true;
            case Manual:
                return false;
        }
        return false;
    }

    public boolean isGeneratingOnly() {
        return this == Generated;
    }

    public boolean isManual() {
        switch(this) {
            case Generated:
                return false;
            case Extended:
            case Manual:
                return true;
        }
        return false;
    }

    public boolean isManualOnly() {
        return this == Manual;
    }
}
