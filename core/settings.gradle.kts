rootProject.name = "spoofax3.core.root"

// This allows us to use plugins from Metaborg Artifacts
pluginManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
    }
}

// This allows us to use the catalog in dependencies
dependencyResolutionManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
    }
    versionCatalogs {
        create("libs") {
            from("org.metaborg.spoofax3:catalog:0.3.3")
        }
    }
}

// This downloads an appropriate JVM if not already available
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include("spoofax.depconstraints")

include("spoofax.common")
include("aterm.common")
include("jsglr.common")
include("jsglr.pie")
include("jsglr1.common")
include("jsglr2.common")
include("esv.common")
include("stratego.common")
include("stratego.pie")
include("constraint.common")
include("constraint.pie")
include("nabl2.common")
include("statix.codecompletion")
include("statix.codecompletion.pie")
include("statix.common")
include("statix.pie")
include("statix.multilang")
include("statix.multilang.eclipse")
include("spt.api")
include("tego.runtime")
include("spoofax2.common")
include("tooling.eclipsebundle")
include("transform.pie")

include("spoofax.core")
include("spoofax.resource")
include("spoofax.test")

include("spoofax.cli")
include("spoofax.intellij")
include("spoofax.eclipse")

include("spoofax.compiler")
include("spoofax.compiler.spoofax2")
include("spoofax.compiler.spoofax2.dagger")
include("spoofax.compiler.interfaces")
include("spoofax.compiler.gradle")
include("spoofax.compiler.gradle.spoofax2")
include("spoofax.compiler.eclipsebundle")
