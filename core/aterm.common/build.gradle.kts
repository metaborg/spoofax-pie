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
    api(libs.spoofax.terms)

    // Depend on Stratego projects for access to standard library strategies which do aterm pretty printing.
    implementation(libs.interpreter.core)
    implementation(libs.strategoxt.strj)

    compileOnly(libs.checkerframework.android)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
