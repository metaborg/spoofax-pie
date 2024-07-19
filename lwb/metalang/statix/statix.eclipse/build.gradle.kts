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

dependencies {
    // Required because @Nullable has runtime retention (which includes classfile retention), and the Java compiler requires access to it.
    compileOnly(libs.jsr305)
}

languageEclipseProject {
    adapterProject.set(project(":statix"))
}

mavenize {
    majorVersion.set("2022-06")
}

tasks {
    "jar"(Jar::class) {
        val exportPackages = LinkedHashSet<String>()
        // Allow split package because `statix.solver` also includes the `mb.statix` package. Add before existing exports so
        // that this takes precedence
        exportPackages.add("mb.statix.*;-split-package:=merge-first")
        val existingExportPackages = manifest.attributes.get("Export-Package")
        if (existingExportPackages != null) {
            exportPackages.add(existingExportPackages.toString())
        }
        val privatePackages = LinkedHashSet<String>()
        val existingPrivatePackages = manifest.attributes.get("Private-Package")
        if (existingPrivatePackages != null) {
            privatePackages.add(existingPrivatePackages.toString())
        }
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
