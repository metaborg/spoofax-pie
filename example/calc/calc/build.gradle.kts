plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.spoofax.lwb.compiler.gradle.language")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
    testImplementation(platform(libs.metaborg.platform))
    testImplementation(libs.spoofax3.test)
    testImplementation(libs.junit)
    testCompileOnly(libs.checkerframework.android)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
