package mb.spoofax.api.style;

import javax.annotation.Nullable;

public interface Style {
    @Nullable Color color();

    @Nullable Color backgroundColor();

    boolean bold();

    boolean italic();

    boolean underscore();

    boolean strikeout();
}
