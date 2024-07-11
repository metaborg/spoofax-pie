plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(project(":spoofax.depconstraints")))
    annotationProcessor(platform(project(":spoofax.depconstraints")))

    api(libs.dagger)
    implementation(project(":spoofax.core"))
    api(libs.picocli)

    compileOnly(libs.checkerframework.android)

    annotationProcessor(libs.dagger.compiler)
    annotationProcessor(libs.picocli.codegen)
}

tasks.compileJava {
    options.compilerArgs.add("-Aproject=${project.group}/${project.name}")
}
