package mb.pipe.run.core.model.style;

import java.io.Serializable;

import mb.pipe.run.core.model.parse.IToken;

public interface ITokenStyle extends Serializable {
    IToken token();

    IStyle style();
}
