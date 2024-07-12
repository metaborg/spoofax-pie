plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

dependencies {
    api(platform(libs.metaborg.platform))
    implementation(platform(libs.metaborg.platform))

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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
