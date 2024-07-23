plugins {
    id("org.metaborg.devenv.spoofax.gradle.langspec")
    `maven-publish`
}

spoofaxLanguageSpecification {
    addCompileDependenciesFromMetaborgYaml.set(false)
    addSourceDependenciesFromMetaborgYaml.set(false)
}

val spoofax2Version: String by ext
val spoofax2DevenvVersion: String by ext
dependencies {
    compileLanguage("org.metaborg:org.metaborg.meta.lang.esv:$spoofax2Version")
    compileLanguage("org.metaborg:org.metaborg.meta.lang.template:$spoofax2Version")
    compileLanguage("org.metaborg:org.metaborg.meta.nabl2.lang:$spoofax2Version")
    compileLanguage("org.metaborg:org.metaborg.meta.nabl2.ext.dynsem:$spoofax2Version")
    compileLanguage("org.metaborg:dynsem:$spoofax2Version")
    compileLanguage("org.metaborg.devenv:org.metaborg.meta.lang.template:$spoofax2DevenvVersion")
    compileLanguage("org.metaborg.devenv:org.metaborg.meta.lang.esv:$spoofax2DevenvVersion")
    compileLanguage("org.metaborg.devenv:statix.lang:$spoofax2DevenvVersion")

    sourceLanguage("org.metaborg:meta.lib.spoofax:$spoofax2Version")
    sourceLanguage("org.metaborg:org.metaborg.meta.nabl2.shared:$spoofax2Version")
    sourceLanguage("org.metaborg:org.metaborg.meta.nabl2.runtime:$spoofax2Version")
    sourceLanguage("org.metaborg.devenv:meta.lib.spoofax:$spoofax2DevenvVersion")
    sourceLanguage("org.metaborg.devenv:statix.runtime:$spoofax2DevenvVersion")
}

