plugins {
    id("org.metaborg.devenv.spoofax.gradle.langspec")
    `maven-publish`
}

val spoofax2DevenvVersion: String by ext
dependencies {
    compileLanguage(libs.statix.lang)
    sourceLanguage(project(":signature-interface.spoofaxcore"))
}

afterEvaluate {
    val importSignature = tasks.register<Sync>("importSignature") {
        from("../signature-interface.spoofaxcore/trans/abstract-sig")
        into("trans/abstract-sig")
    }
    tasks.getByName("spoofaxBuildLanguageSpec") {
        dependsOn(importSignature)
    }
}
