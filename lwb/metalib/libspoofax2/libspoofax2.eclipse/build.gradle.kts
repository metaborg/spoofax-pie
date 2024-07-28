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

languageEclipseProject {
    adapterProject.set(project(":libspoofax2"))
}

mavenize {
    majorVersion.set("2022-06")
}
