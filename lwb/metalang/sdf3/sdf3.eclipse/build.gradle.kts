plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.spoofax.compiler.gradle.eclipse")
}

languageEclipseProject {
    adapterProject.set(project(":sdf3"))
}

tasks {
    "jar"(Jar::class) {
        val exportPackages = LinkedHashSet<String>()
        val existingExportPackages = manifest.attributes.get("Export-Package")
        if (existingExportPackages != null) {
            exportPackages.add(existingExportPackages.toString())
        }
        val privatePackages = LinkedHashSet<String>()
        val existingPrivatePackages = manifest.attributes.get("Private-Package")
        if (existingPrivatePackages != null) {
            privatePackages.add(existingPrivatePackages.toString())
        }
        privatePackages.add("org.metaborg.sdf2parenthesize.*") // Embed `sdf2parenthesize` because `sdf3` depends on it.
        manifest {
            attributes(
                Pair("Export-Package", exportPackages.joinToString(", ")),
                Pair("Private-Package", privatePackages.joinToString(", "))
            )
        }
    }
}

mavenize {
    majorVersion.set("2022-06")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
