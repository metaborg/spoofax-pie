package mb.common.style;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public interface Style extends Serializable {
    @Nullable Color getColor();

    @Nullable Color getBackgroundColor();

    boolean isBold();

    boolean isItalic();

    boolean isUnderscore();

    boolean isStrikeout();
}
