package mb.tiger;

import mb.nabl2.common.NaBL2PrimitiveLibrary;
import mb.stratego.common.StrategoRuntimeBuilder;

public class TigerNaBL2StrategoRuntimeBuilder {
    public static StrategoRuntimeBuilder create(StrategoRuntimeBuilder builder) {
        builder.addLibrary(new NaBL2PrimitiveLibrary());
        return builder;
    }
}
