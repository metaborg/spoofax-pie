plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.spoofax.compiler.gradle.eclipse")
}

languageEclipseProject {
    adapterProject.set(project(":libspoofax2"))
}

mavenize {
    majorVersion.set("2022-06")
}
