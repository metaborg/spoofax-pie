plugins {
    id("org.metaborg.coronium.repository")
    `maven-publish`
}

mavenize {
    majorVersion.set("2022-06")
}

repository {
    eclipseInstallationAppName.set("Spoofax3")
    createEclipseInstallationPublications.set(true)
    createEclipseInstallationWithJvmPublications.set(true)
}

// This is a copy of dependencyManagement in the root project's settings.gradle.kts,
//  which is needed because the Mavenize plugin defined its own repository,
//  overriding those defined in the root dependencyManagement.
repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
    mavenCentral()
}

dependencies {
    feature(project(":spoofax.lwb.eclipse.feature"))
}

tasks {
    withType<mb.coronium.task.EclipseRun> {
        jvmArgs("-Xss16M") // Set required stack size, mainly for serialization.
    }
//  withType<mb.coronium.task.EclipseCreateInstallation> {
//    baseRepositories.set(listOf(
//      "https://artifacts.metaborg.org/content/groups/eclipse-2021-09/"
//    ))
//  }
}
