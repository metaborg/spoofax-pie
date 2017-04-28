package mb.pipe.run.core.model.parse;

import java.io.Serializable;

public interface ITokenType extends Serializable {
    void accept(ITokenKindVisitor visitor, IToken token);
}
