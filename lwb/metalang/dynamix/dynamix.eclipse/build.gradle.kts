plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.spoofax.compiler.gradle.eclipse")
}

// This is a copy of dependencyManagement in the root project's settings.gradle.kts,
//  which is needed because the Mavenize plugin (via Spoofax.Compiler applying Coronium) defines its own repository,
//  overriding those defined in the root dependencyManagement.
repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
    mavenCentral()
}

dependencies {
    // Required because @Nullable has runtime retention (which includes classfile retention), and the Java compiler requires access to it.
    compileOnly(libs.jsr305)

    // Depend on `spoofax.compiler.eclipsebundle` because `cfg` uses `spoofax.compiler` and `spoofax.compiler.dagger`.
    bundleApi(libs.spoofax3.compiler.eclipsebundle)
}

languageEclipseProject {
    adapterProject.set(project(":dynamix"))
}

mavenize {
    majorVersion.set("2022-06")
}
