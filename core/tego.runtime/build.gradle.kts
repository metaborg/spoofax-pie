plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

dependencies {
    api(platform(libs.metaborg.platform))

    api(libs.metaborg.common)
    api(libs.metaborg.log.api)

    api(libs.metaborg.log.dagger)
    api(libs.dagger)
    annotationProcessor(libs.dagger.compiler)

    compileOnly(libs.checkerframework.android)

    testImplementation(libs.junit)
    testCompileOnly(libs.checkerframework.android)
    testImplementation(libs.metaborg.log.backend.slf4j)
    testImplementation(libs.slf4j.simple)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
