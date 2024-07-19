
// FIXME: org.metaborg:strategolib.eclipse in Spoofax 3 conflicts with a same named package in Spoofax 2 (from Stratego)
//  Use a devenv prefix?

plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.spoofax.compiler.gradle.eclipse")
}

// This is a copy of dependencyManagement in the root project's settings.gradle.kts,
//  which is needed because the Mavenize plugin (via Spoofax.Compiler applying Coronium) defines its own repository,
//  overriding those defined in the root dependencyManagement.
repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
    mavenCentral()
}

languageEclipseProject {
    adapterProject.set(project(":strategolib"))
}

mavenize {
    majorVersion.set("2022-06")
}

tasks {
    "jar"(Jar::class) {
        val exportPackages = LinkedHashSet<String>()
        // Export `strategolib` package.
        exportPackages.add("strategolib.*")
        val existingExportPackages = manifest.attributes.get("Export-Package")
        if (existingExportPackages != null) {
            exportPackages.add(existingExportPackages.toString())
        }
        manifest {
            attributes(
                Pair("Export-Package", exportPackages.joinToString(", "))
            )
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
