plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.devenv.spoofax.gradle.base")
}

sourceSets {
    main {
        java {
            srcDir("$buildDir/generated/sources/spoofax/java")
        }
    }
}

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    api(libs.metaborg.common)
    api(libs.spoofax3.compiler.interfaces)
    api(libs.spoofax3.resource)
    api(libs.spoofax3.jsglr1.common)
    api(libs.spoofax3.esv.common)
    api(libs.spoofax3.stratego.common)
    api(libs.spoofax3.constraint.common)

    implementation(libs.spoofax3.nabl2.common)
    implementation(libs.spoofax2.strategoxt.minjar)

    compileOnly(libs.checkerframework.android)

    testImplementation(libs.spoofax3.test)
    testImplementation(libs.junit)
    testCompileOnly(libs.checkerframework.android)
}

fun copySpoofaxLanguageResources(
    dependency: Dependency,
    destinationPackage: String,
    includeStrategoClasses: Boolean,
    vararg resources: String,
) {
    val allResources = resources.toMutableList()
    if (includeStrategoClasses) {
        allResources.add("target/metaborg/stratego.jar")
    }

    // Add language dependency.
    dependencies.add("compileLanguage", dependency)

    // Unpack the '.spoofax-language' archive.
    val languageFiles = project.configurations.getByName("languageFiles")
    val unpackSpoofaxLanguageDir = "$buildDir/unpackedSpoofaxLanguage/"
    val unpackSpoofaxLanguageTask = tasks.register<Sync>("unpackSpoofaxLanguage") {
        dependsOn(languageFiles)
        from({ languageFiles.map { project.zipTree(it) } })  /* Closure inside `from` to defer evaluation until task execution time */
        into(unpackSpoofaxLanguageDir)
        include(allResources)
    }
    // Copy resources into `mainSourceSet.java.outputDir` and `testSourceSet.java.outputDir`, so they end up in the target package.
    val resourcesCopySpec = copySpec {
        from(unpackSpoofaxLanguageDir)
        include(*resources)
    }
    val strategoCopySpec = copySpec {
        from(project.zipTree("$unpackSpoofaxLanguageDir/target/metaborg/stratego.jar"))
        exclude("META-INF")
    }
    val copyMainTask = tasks.register<Copy>("copyMainResources") {
        dependsOn(unpackSpoofaxLanguageTask)
        into(sourceSets.main.get().java.outputDir)
        into(destinationPackage) { with(resourcesCopySpec) }
        if (includeStrategoClasses) {
            into(".") { with(strategoCopySpec) }
        }
    }
    tasks.getByName(JavaPlugin.CLASSES_TASK_NAME).dependsOn(copyMainTask)
    val copyTestTask = tasks.register<Copy>("copyTestResources") {
        dependsOn(unpackSpoofaxLanguageTask)
        into(sourceSets.test.get().java.outputDir)
        into(destinationPackage) { with(resourcesCopySpec) }
        if (includeStrategoClasses) {
            into(".") { with(strategoCopySpec) }
        }
    }
    tasks.getByName(JavaPlugin.TEST_CLASSES_TASK_NAME).dependsOn(copyTestTask)
}
copySpoofaxLanguageResources(
    dependencies.create("org.metaborg.devenv:tiger.spoofaxcore:$version"),
    "mb/tiger",
    true,
    "target/metaborg/editor.esv.af", "target/metaborg/sdf.tbl"
)

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
