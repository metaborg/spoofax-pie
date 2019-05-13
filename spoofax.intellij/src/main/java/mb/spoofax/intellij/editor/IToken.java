package mb.spoofax.intellij.editor;

import mb.spoofax.intellij.ScopeNames;
import mb.spoofax.intellij.Span;

import java.io.Serializable;


public interface IToken extends Serializable {
    Span getLocation();
    ScopeNames getScopes();
}
