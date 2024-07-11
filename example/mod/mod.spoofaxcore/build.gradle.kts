plugins {
    id("org.metaborg.devenv.spoofax.gradle.langspec")
    `maven-publish`
}

val spoofax2DevenvVersion: String by ext
dependencies {
    compileLanguage(libs.sdf3.lang)
    compileLanguage(libs.esv.lang)
    compileLanguage(libs.statix.lang)
    sourceLanguage(libs.spoofax2.meta.lib.spoofax)
    sourceLanguage(libs.statix.runtime)
}
