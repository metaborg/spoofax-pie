plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.coronium.bundle")
}

mavenize {
    majorVersion.set("2022-06")
}

// This is a copy of dependencyManagement in the root project's settings.gradle.kts,
//  which is needed because the Mavenize plugin defined its own repository,
//  overriding those defined in the root dependencyManagement.
repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
    mavenCentral()
}

dependencies {
    bundleTargetPlatformApi(eclipse("javax.inject"))
    bundleTargetPlatformApi(eclipse("org.eclipse.jdt.core"))


    bundleApi(libs.spoofax3.eclipse)
    bundleApi(libs.spoofax3.tooling.eclipsebundle)
    bundleApi(libs.spoofax3.compiler.eclipsebundle)


    bundleImplementation(libs.spoofax3.cfg.eclipse)
    bundleImplementation(libs.spoofax3.sdf3.eclipse)
    bundleImplementation(libs.spoofax3.esv.eclipse)
    bundleImplementation(libs.spoofax3.stratego.eclipse)
    bundleImplementation(libs.spoofax3.statix.eclipse)
    bundleImplementation(libs.spoofax3.dynamix.eclipse)
    bundleImplementation(libs.spoofax3.sdf3.extstatix.eclipse)
    bundleImplementation(libs.spoofax3.sdf3.extdynamix.eclipse)
    bundleImplementation(libs.spoofax3.spt.eclipse)

    bundleImplementation(libs.spoofax3.strategolib.eclipse)
    bundleImplementation(libs.spoofax3.gpp.eclipse)
    bundleImplementation(libs.spoofax3.libspoofax2.eclipse)
    bundleImplementation(libs.spoofax3.libstatix.eclipse)

    bundleImplementation(project(":rv32im.eclipse"))


    // Convenient library to get the current classpath, which works under OSGi (Eclipse) as well. Used to pass the current
    // classpath to the Java compiler.
    // TODO: only using this to extract a classpath, can we just copy that functionality without a dependency?
    bundleEmbedImplementation(libs.classgraph)

    bundleEmbedImplementation(libs.dagger.compiler) {
        // Exclude `checker-qual` in favor of `checker-qual-android` which has classfile retention instead of runtime.
        exclude("org.checkerframework", "checker-qual")
    }

    // HACK: embed javax.inject as classgraph does not seem to pick up the above javax.inject dependency?
    bundleEmbedImplementation(libs.javax.inject)

    // Embed `:spoofax.lwb.dynamicloading`.
    bundleEmbedImplementation(libs.spoofax3.lwb.dynamicloading) {
        // Exclude modules already exported by `spoofax.eclipse`
        exclude("org.metaborg", "common")
        exclude("org.metaborg", "spoofax.core")
        exclude("org.metaborg", "log.api")
        exclude("org.metaborg", "resource")
        exclude("org.metaborg", "pie.api")
        exclude("org.metaborg", "pie.runtime")
        exclude("com.google.dagger", "dagger")

        // Exclude dagger-compile because we already manually embed it.
        exclude("com.google.dagger", "dagger-compiler")
    }

    // Embed `:spoofax.lwb.compiler`.
    bundleEmbedImplementation(libs.spoofax3.lwb.compiler) {
        // Exclude meta-languages and libraries, as they have their own Eclipse plugins
        exclude("org.metaborg", "cfg")
        exclude("org.metaborg", "sdf3")
        exclude("org.metaborg", "esv")
        exclude("org.metaborg", "stratego")
        exclude("org.metaborg", "statix")
        exclude("org.metaborg", "sdf3_ext_statix")
        exclude("org.metaborg", "spt")

        exclude("org.metaborg", "strategolib")
        exclude("org.metaborg", "gpp")
        exclude("org.metaborg", "libspoofax2")
        exclude("org.metaborg", "libstatix")

        // Exclude modules already exported by `spoofax.eclipse`
        exclude("org.metaborg", "common")
        exclude("org.metaborg", "spoofax.core")
        exclude("org.metaborg", "log.api")
        exclude("org.metaborg", "resource")
        exclude("org.metaborg", "pie.api")
        exclude("org.metaborg", "pie.runtime")
        exclude("com.google.dagger", "dagger")

        // Exclude modules already exported by `tooling.eclipsebundle`
        exclude("org.metaborg", "spoofax.compiler.interfaces")

        // Exclude modules already exported by `spoofax.compiler.eclipsebundle`
        exclude("org.metaborg", "spoofax.compiler")

        // Exclude dagger-compile because we already manually embed it.
        exclude("com.google.dagger", "dagger-compiler")
    }

    // Embed `:spt.dynamicloading`.
    bundleEmbedImplementation(libs.spoofax3.spt.dynamicloading)

    // Embed `org.metaborg:pie.task.archive` and `org.metaborg:pie.task.java`
    bundleEmbedImplementation(libs.metaborg.pie.task.archive)
    bundleEmbedImplementation(libs.metaborg.pie.task.java)

    // Embed FST serialize/deserialize implementation.
    bundleEmbedImplementation(libs.metaborg.pie.serde.fst)

    compileOnly(libs.checkerframework.android)

    annotationProcessor(libs.dagger.compiler)
}

val privatePackage = listOf(
    // Our own packages should not be private.
    "!mb.spoofax.lwb.eclipse",
    "!mb.spoofax.lwb.eclipse.*",
    // Allow split packages for `mb.spoofax.lwb.compiler` because `spoofax.lwb.compiler.dagger` generates dagger classes
    // in the same package
    "mb.spoofax.lwb.compiler.*;-split-package:=merge-first",
    // Embed `mb.spoofax.lwb`, `mb.spoofax.lwb.dynamicloading`, and co.
    "mb.spoofax.lwb.*",
    // Embed `mb.spt.dynamicloading`
    "mb.spt.dynamicloading.*",
    // Embed `javax.inject`
    "javax.inject.*",
    // Embed `org.checkerframework:checker-qual-android`
    "org.checkerframework.*",
    // Embed `com.google.dagger:dagger-compiler` and dependencies. Allow split packages for `dagger.model`, as
    // `dagger-compiler` and `dagger-spi` both have this package
    "dagger.model.*;-split-package:=merge-first",
    "dagger.*",
    "dagger.internal.*",
    "com.google.common.*",
    "com.google.common.util.concurrent.internal.*",
    "com.squareup.javapoet.*",
    "kotlin.*;-split-package:=merge-first",
    "javax.annotation.*;-split-package:=merge-first",
    // Embed `io.github.classgraph:classgraph`.
    "io.github.classgraph.*",
    "nonapi.io.github.classgraph.*",
    // Embed PIE task modules
    "mb.pie.task.archive.*",
    "mb.pie.task.java.*",
    // Embed PIE FST Serde, FST, and its dependencies.
    "mb.pie.serde.fst.*",
    "org.nustaq.*",
    "com.fasterxml.jackson.core.*",
    "org.objenesis.*",
    // Embed services, to make the embedded Dagger annotation processor work.
    "META-INF.services.*;-split-package:=merge-first"
)
tasks {
    "jar"(Jar::class) {
        manifest {
            attributes(
                Pair("Private-Package", privatePackage.joinToString(", "))
            )
        }
    }
    withType<mb.coronium.task.EclipseRun> {
        jvmArgs("-Xss16M") // Set required stack size, mainly for serialization.
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
