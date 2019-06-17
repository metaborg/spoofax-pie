package mb.tiger;

import mb.stratego.common.StrategoRuntimeBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.URL;

public class TigerStrategoRuntimeBuilder {
    public static StrategoRuntimeBuilder fromClassLoaderResources() {
        final String jarResource = "mb/tiger/target/metaborg/stratego.jar";
        final @Nullable URL jar = TigerStrategoRuntimeBuilder.class.getClassLoader().getResource(jarResource);
        if(jar == null) {
            throw new RuntimeException(
                "Cannot create Tiger Stratego runtime; cannot find resource '" + jarResource + "' in classloader resources");
        }

        final String javastratJarResource = "mb/tiger/target/metaborg/stratego-javastrat.jar";
        final @Nullable URL javastratJar =
            TigerStrategoRuntimeBuilder.class.getClassLoader().getResource(javastratJarResource);
        if(javastratJar == null) {
            throw new RuntimeException(
                "Cannot create Tiger Stratego runtime; cannot find resource '" + javastratJarResource + "' in classloader resources");
        }

        final StrategoRuntimeBuilder builder = new StrategoRuntimeBuilder();
        builder.addJar(jar);
        builder.addJar(javastratJar);
        builder.withJarParentClassLoader(TigerStrategoRuntimeBuilder.class.getClassLoader());
        return builder;
    }
}
