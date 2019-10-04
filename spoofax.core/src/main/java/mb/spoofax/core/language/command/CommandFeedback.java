package mb.spoofax.core.language.command;

import mb.common.region.Region;
import mb.common.util.ADT;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Encodes the result of a command.
 */
@ADT
public abstract class CommandFeedback implements Serializable {

    /**
     * The cases of the abstract data type.
     *
     * @param <R> The return type of the case handler.
     */
    public interface Cases<R> {

        /**
         * Case when the command returned a resource.
         *
         * @param resource The resource key.
         * @param region   The region to select in the resource; or {@code null} to select nothing.
         * @return The return value of the case handler.
         */
        R showFile(ResourceKey resource, @Nullable Region region);

        // TODO: This should return a resource as well.
        /**
         * Case when the command returned a text resource.
         *
         * @param text   The resource text.
         * @param name   The resource name.
         * @param region The region to select in the resource; or {@code null} to select nothing.
         * @return The return value of the case handler.
         */
        R showText(String text, String name, @Nullable Region region);

    }

    /**
     * Matches the value and executes a case handler.
     *
     * @param cases The case handler.
     * @param <R>   The return type of the case handler.
     * @return The return value of the case handler.
     */
    public abstract <R> R match(Cases<R> cases);

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(@Nullable Object obj);

    @Override
    public abstract String toString();

}
