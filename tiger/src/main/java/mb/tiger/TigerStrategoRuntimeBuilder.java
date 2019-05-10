package mb.tiger;

import mb.stratego.common.StrategoRuntimeBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.URL;

public class TigerStrategoRuntimeBuilder extends StrategoRuntimeBuilder {
    public static TigerStrategoRuntimeBuilder fromClassLoaderResources() {
        final String jarResource = "mb/tiger/stratego.jar";
        final @Nullable URL jar = TigerStrategoRuntimeBuilder.class.getClassLoader().getResource(jarResource);
        if(jar == null) {
            throw new RuntimeException(
                "Cannot create Tiger Stratego runtime; cannot find resource '" + jarResource + "' in classloader resources");
        }

        final String javastratJarResource = "mb/tiger/stratego-javastrat.jar";
        final @Nullable URL javastratJar = TigerStrategoRuntimeBuilder.class.getClassLoader().getResource(jarResource);
        if(javastratJar == null) {
            throw new RuntimeException(
                "Cannot create Tiger Stratego runtime; cannot find resource '" + javastratJarResource + "' in classloader resources");
        }

        final TigerStrategoRuntimeBuilder builder = new TigerStrategoRuntimeBuilder();
        builder.addJar(jar);
        builder.addJar(javastratJar);
        return builder;
    }
}
