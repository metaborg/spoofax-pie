package mb.tiger;

import mb.spoofax.compiler.interfaces.spoofaxcore.StrategoRuntimeBuilderFactory;
import mb.stratego.common.StrategoRuntimeBuilder;

public class TigerStrategoRuntimeBuilderFactory implements StrategoRuntimeBuilderFactory {
    @Override public StrategoRuntimeBuilder create() {
        return TigerNaBL2StrategoRuntimeBuilder.create(TigerStrategoRuntimeBuilder.create());
    }
}
