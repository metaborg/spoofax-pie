package mb.pipe.run.core.model;

import java.io.Serializable;

import javax.annotation.Nullable;

/**
 * Interface representing a message on a region in a source file.
 */
public interface IMsg extends Serializable {
    /**
     * @return Message text.
     */
    String text();

    /**
     * @return Message severity
     */
    IMsgSeverity severity();

    /**
     * @return Message type.
     */
    IMsgType type();

    /**
     * @return Affected region, or null if it is unknown.
     */
    @Nullable IRegion region();

    /**
     * @return Exception belonging to this message, or null if there is no exception.
     */
    @Nullable Throwable exception();
}
