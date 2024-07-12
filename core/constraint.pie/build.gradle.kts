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
    implementation(project(":aterm.common"))
    api(project(":constraint.common"))
    api(project(":spoofax.core"))
    api(libs.metaborg.pie.api)

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
