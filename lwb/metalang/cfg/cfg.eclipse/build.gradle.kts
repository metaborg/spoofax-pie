plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.spoofax.compiler.gradle.eclipse")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
    // Required because @Nullable has runtime retention (which includes classfile retention), and the Java compiler requires access to it.
    compileOnly(libs.jsr305)

    // Depend on `spoofax.compiler.eclipsebundle` because `cfg` uses `spoofax.compiler` and `spoofax.compiler.dagger`.
    bundleApi(libs.spoofax3.compiler.eclipsebundle)
}

languageEclipseProject {
    adapterProject.set(project(":cfg"))
}

mavenize {
    majorVersion.set("2022-06")
}
