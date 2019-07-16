package mb.tiger;

import mb.stratego.common.StrategoRuntimeBuilder;

public class TigerStrategoRuntimeBuilder {
    public static StrategoRuntimeBuilder create() {
        final StrategoRuntimeBuilder builder = new StrategoRuntimeBuilder();
        builder.addInteropRegistererByReflection("org.metaborg.lang.tiger.trans.InteropRegisterer");
        builder.addInteropRegistererByReflection("org.metaborg.lang.tiger.strategies.InteropRegisterer");
        builder.withJarParentClassLoader(TigerStrategoRuntimeBuilder.class.getClassLoader());
        return builder;
    }
}
