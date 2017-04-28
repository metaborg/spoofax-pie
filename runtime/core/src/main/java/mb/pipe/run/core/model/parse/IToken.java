package mb.pipe.run.core.model.parse;

import java.io.Serializable;

import mb.pipe.run.core.model.region.IRegion;

public interface IToken extends Serializable {
    IRegion region();

    ITokenType type();

    String text();
}
