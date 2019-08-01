package mb.spoofax.core.language.transform;

import java.io.Serializable;

public enum TransformExecutionType implements Serializable {
    /**
     * Manually require transform once, without observing it. Can be executed in editor and resource context. {@link
     * TransformFeedback Feedback} is handled normally.
     */
    ManualOnce,
    /**
     * Manually require and observe transform, and unobserve it whenever an editor opened through feedback ({@link
     * TransformFeedback.Cases#openEditorForFile}, {@link TransformFeedback.Cases#openEditorWithText}) is closed. Can
     * only be executed in file or editor context. Feedback is handled normally, but only open editor feedback is
     * supported.
     */
    ManualContinuous,
    /**
     * Automatically require and observe transform whenever a resource is added or changed, and unobserve it whenever a
     * resource is removed. Cannot be manually executed. {@link TransformFeedback Feedback} is ignored.
     */
    AutomaticContinuous
}
