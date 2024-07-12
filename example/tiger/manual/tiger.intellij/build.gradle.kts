plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.jetbrains.intellij")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
    implementation(platform(libs.metaborg.platform))

    implementation(libs.metaborg.pie.runtime)
    implementation(libs.spoofax3.core)
    implementation(libs.spoofax3.intellij)
    implementation(project(":tiger.spoofax")) {
        exclude(group = "org.slf4j")
    }

    implementation(libs.dagger)

    compileOnly(libs.checkerframework.android)

    annotationProcessor(libs.dagger.compiler)
}

intellij {
    version.set("2020.2.4") // 2020.2.4 is the last version that can be built with Java 8.
    instrumentCode.set(false) // Skip non-incremental and slow code instrumentation.
}

tasks {
    named("buildSearchableOptions") {
        enabled = false // Skip non-incremental and slow `buildSearchableOptions` task from `org.jetbrains.intellij`.
    }

    named<org.jetbrains.intellij.tasks.RunIdeTask>("runIde") {
        jbrVersion.set("11_0_2b159") // Set JBR version because the latest one cannot be downloaded.
        // HACK: make task depend on the runtime classpath to forcefully make it depend on `spoofax.intellij`, which the
        //       `org.jetbrains.intellij` plugin seems to ignore. This is probably because `spoofax.intellij` is a plugin
        //       but is not listed as a plugin dependency. This hack may not work when publishing this plugin.
        dependsOn(configurations.runtimeClasspath)
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
