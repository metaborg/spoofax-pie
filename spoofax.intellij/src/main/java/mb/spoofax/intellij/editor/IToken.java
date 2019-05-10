package mb.spoofax.intellij.editor;

import java.io.Serializable;


public interface IToken extends Serializable {
    Span getLocation();
    ScopeNames getScopes();
}
