package mb.pipe.run.core.model.message;

import java.io.Serializable;

import javax.annotation.Nullable;

import mb.pipe.run.core.model.region.Region;

/**
 * Interface representing a message on a region in a source file.
 */
public interface Msg extends Serializable {
    /**
     * @return Message text.
     */
    String text();

    /**
     * @return Message severity
     */
    MsgSeverity severity();

    /**
     * @return Message type.
     */
    MsgType type();

    /**
     * @return Affected region, or null if it is unknown.
     */
    @Nullable Region region();

    /**
     * @return Exception belonging to this message, or null if there is no exception.
     */
    @Nullable Throwable exception();
}
