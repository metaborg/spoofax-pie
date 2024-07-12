plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

dependencies {
    api(platform(libs.metaborg.platform))
    implementation(platform(libs.metaborg.platform))

    api(libs.metaborg.common)
    api(libs.metaborg.log.api)
    api(libs.metaborg.resource.api)

    implementation(project(":aterm.common"))
    implementation(project(":jsglr.common"))

    api(libs.spoofax.terms)
    api(libs.interpreter.core)
    api(libs.strategoxt.strj)

    compileOnly(libs.checkerframework.android)
    compileOnly(libs.derive4j.annotation)

    annotationProcessor(libs.derive4j)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
