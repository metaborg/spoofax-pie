plugins {
    id("org.metaborg.devenv.spoofax.gradle.langspec")
    `maven-publish`
}

val spoofax2DevenvVersion: String by ext
spoofaxLanguageSpecification {
    addSourceDependenciesFromMetaborgYaml.set(false)
    addCompileDependenciesFromMetaborgYaml.set(false)
}
dependencies {
    compileLanguage(libs.sdf3.lang)
    compileLanguage(libs.esv.lang)
    compileLanguage(libs.statix.lang)
    compileLanguage(libs.sdf3.extstatix)

    sourceLanguage(libs.spoofax2.meta.lib.spoofax)
    sourceLanguage(libs.statix.runtime)
    sourceLanguage(project(":signature-interface.spoofaxcore"))
    sourceLanguage(project(":module-interface.spoofaxcore"))
}

afterEvaluate {
    val importSignature = tasks.register<Sync>("importSignature") {
        from("../signature-interface.spoofaxcore/trans/abstract-sig")
        into("trans/abstract-sig")
    }
    val importModule = tasks.register<Sync>("importModule") {
        from("../module-interface.spoofaxcore/trans/modules")
        into("trans/modules")
    }
    tasks.getByName("spoofaxBuildLanguageSpec") {
        dependsOn(importSignature)
        dependsOn(importModule)
    }
}
