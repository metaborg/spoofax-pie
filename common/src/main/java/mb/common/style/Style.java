package mb.common.style;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public interface Style extends Serializable {
    @Nullable Color color();

    @Nullable Color backgroundColor();

    boolean bold();

    boolean italic();

    boolean underscore();

    boolean strikeout();
}
