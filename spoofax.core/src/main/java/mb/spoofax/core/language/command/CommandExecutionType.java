package mb.spoofax.core.language.command;

import java.io.Serializable;


/**
 * Specifies the type of command execution.
 */
public enum CommandExecutionType implements Serializable {
    /**
     * Manually require command once, without observing it. Can be executed in editor and resource context. {@link
     * CommandFeedback Feedback} is handled normally.
     */
    ManualOnce,
    /**
     * Manually require and observe command, and unobserve it whenever an editor opened through feedback ({@link
     * CommandFeedback.Cases#showFile}, {@link CommandFeedback.Cases#showText}) is closed. Can only be executed in file
     * or editor context. Feedback is handled normally, but only open editor feedback is supported.
     */
    ManualContinuous,
    /**
     * Automatically require and observe command whenever a resource is added or changed, and unobserve it whenever a
     * resource is removed. Cannot be manually executed. {@link CommandFeedback Feedback} is ignored.
     */
    AutomaticContinuous
}
