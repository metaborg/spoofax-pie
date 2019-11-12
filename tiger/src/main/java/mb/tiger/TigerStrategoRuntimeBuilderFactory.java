package mb.tiger;

import mb.nabl2.common.NaBL2PrimitiveLibrary;
import mb.spoofax.compiler.interfaces.spoofaxcore.StrategoRuntimeBuilderFactory;
import mb.stratego.common.StrategoRuntimeBuilder;

public class TigerStrategoRuntimeBuilderFactory implements StrategoRuntimeBuilderFactory {
    @Override public StrategoRuntimeBuilder create() {
        final StrategoRuntimeBuilder builder = new StrategoRuntimeBuilder();
        builder.addInteropRegistererByReflection("org.metaborg.lang.tiger.trans.InteropRegisterer");
        builder.addInteropRegistererByReflection("org.metaborg.lang.tiger.strategies.InteropRegisterer");
        builder.withJarParentClassLoader(TigerStrategoRuntimeBuilderFactory.class.getClassLoader());
        builder.addLibrary(new NaBL2PrimitiveLibrary());
        return builder;
    }
}
