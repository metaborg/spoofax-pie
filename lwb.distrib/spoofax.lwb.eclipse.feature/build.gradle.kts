plugins {
    id("org.metaborg.coronium.feature")
}

mavenize {
    majorVersion.set("2022-06")
}

// This is a copy of dependencyManagement in the root project's settings.gradle.kts,
//  which is needed because the Mavenize plugin defined its own repository,
//  overriding those defined in the root dependencyManagement.
repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
    mavenCentral()
}

dependencies {
    bundle(project(":spoofax.lwb.eclipse"))
    bundle(project(":rv32im.eclipse"))
}

tasks {
    withType<mb.coronium.task.EclipseRun> {
        jvmArgs("-Xss16M") // Set required stack size, mainly for serialization.
    }
}
