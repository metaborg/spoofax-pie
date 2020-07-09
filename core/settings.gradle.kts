rootProject.name = "spoofax.core.root"

pluginManagement {
  repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
  }
}

if(org.gradle.util.VersionNumber.parse(gradle.gradleVersion).major < 6) {
  enableFeaturePreview("GRADLE_METADATA")
}

include("spoofax.depconstraints")

include("common")

include("completions.common")
include("jsglr.common")
include("jsglr1.common")
include("jsglr1.pie")
include("jsglr2.common")
include("esv.common")
include("stratego.common")
include("stratego.pie")
include("constraint.common")
include("constraint.pie")
include("nabl2.common")
include("statix.common")
include("statix.multilang")
include("statix.multilang.eclipse")
include("statix.multilang.eclipse.externaldeps")
include("spoofax2.common")

include("spoofax.core")
include("spoofax.cli")
include("spoofax.intellij")
include("spoofax.eclipse")
include("spoofax.eclipse.externaldeps")
include("spoofax.compiler")
include("spoofax.compiler.interfaces")
include("spoofax.compiler.gradle")
