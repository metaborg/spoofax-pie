rootProject.name = "spoofax3.core.root"

pluginManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
        mavenCentral()
    }
}

plugins {
    id("org.metaborg.convention.settings") version "0.6.12"
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
