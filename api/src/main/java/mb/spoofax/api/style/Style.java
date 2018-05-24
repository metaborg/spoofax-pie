package mb.spoofax.api.style;

import java.awt.Color;

import javax.annotation.Nullable;

public interface Style {
    @Nullable Color color();

    @Nullable Color backgroundColor();

    boolean bold();

    boolean italic();

    boolean underscore();

    boolean strikeout();
}
