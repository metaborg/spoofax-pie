plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.spoofax.compiler.gradle.eclipse")
}

mavenize {
    majorVersion.set("2022-06")
}

languageEclipseProject {
    adapterProject.set(project(":rv32im"))
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
        // Embed `edu.berkeley.eecs.venus164:venus164` and `org.jetbrains.kotlin:kotlin-stdlib` because `rv32im` depends on it.
        privatePackages.add("venus.*")
        privatePackages.add("kotlin.*")
        manifest {
            attributes(
                Pair("Export-Package", exportPackages.joinToString(", ")),
                Pair("Private-Package", privatePackages.joinToString(", "))
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
