package mb.spoofax.compiler.util;

public enum ClassKind {
    /**
     * Generated class.
     */
    Generated,
    /**
     * Manually implemented class.
     */
    Manual,
    /**
     * Manually implemented class that extends generated class.
     */
    Extended
}
