package mb.tiger.intellij;

import mb.spoofax.intellij.editor.SpoofaxLexerFactory;
import mb.tiger.intellij.TigerPlugin;
import mb.tiger.spoofax.TigerScope;

import javax.inject.Inject;

@TigerScope
public class TigerLexerFactory extends SpoofaxLexerFactory {
    @Inject public TigerLexerFactory() {
        super(TigerPlugin.getComponent(), TigerPlugin.getResourceServiceComponent(), TigerPlugin.getPieComponent());
    }
}
