plugins {
  `java-platform`
  `maven-publish`
}

val commonVersion = "0.6.1"
val logVersion = "0.5.0"
val slf4jVersion = "1.7.30"
val resourceVersion = "0.10.0"
val pieVersion = "0.14.1"

val spoofax2Version: String by ext
val spoofax2DevenvVersion: String by ext

val picocliVersion = "4.5.0"

val javaxInjectVersion = "1"
val checkerframeworkVersion = "3.6.0"

val daggerVersion = "2.31.2"
val derive4jVersion = "1.1.1"
val immutablesVersion = "2.8.2"

val yamlVersion = "1.26"

dependencies {
  constraints {
    // Own projects
    api(project(":completions.common"))
    api(project(":jsglr.common"))
    api(project(":jsglr1.common"))
    api(project(":jsglr1.pie"))
    api(project(":jsglr2.common"))
    api(project(":esv.common"))
    api(project(":stratego.common"))
    api(project(":constraint.common"))
    api(project(":nabl2.common"))
    api(project(":statix.common"))
    api(project(":statix.multilang"))
    api(project(":spoofax2.common"))

    api(project(":spoofax.core"))
    api(project(":spoofax.cli"))
    api(project(":spoofax.intellij"))
    api(project(":spoofax.eclipse")) // TODO: bundle versions are not picked up when consuming this platform
    api(project(":spoofax.compiler"))
    api(project(":spoofax.compiler.interfaces"))
    api(project(":spoofax.compiler.gradle"))


    // Main dependencies
    /// Common
    api("org.metaborg:common:$commonVersion")
    /// Log
    api("org.metaborg:log.api:$logVersion")
    api("org.metaborg:log.backend.logback:$logVersion")
    api("org.metaborg:log.backend.slf4j:$logVersion")
    /// SLF4j
    api("org.slf4j:slf4j-simple:$slf4jVersion")
    /// Resource
    api("org.metaborg:resource:$resourceVersion")
    /// PIE
    api("org.metaborg:pie.api:$pieVersion")
    api("org.metaborg:pie.runtime:$pieVersion")
    api("org.metaborg:pie.dagger:$pieVersion")
    api("org.metaborg:pie.task.java:$pieVersion")
    api("org.metaborg:pie.task.archive:$pieVersion")
    /// Spoofax 2.x
    runtime("org.metaborg:strategoxt-min-jar:$spoofax2Version")
    /// Spoofax 2.x with devenv override
    api("org.metaborg.devenv:org.strategoxt.strj:$spoofax2DevenvVersion")
    api("org.metaborg.devenv:org.spoofax.terms:$spoofax2DevenvVersion")
    api("org.metaborg.devenv:org.metaborg.util:$spoofax2DevenvVersion")
    api("org.metaborg.devenv:org.spoofax.interpreter.core:$spoofax2DevenvVersion")
    api("org.metaborg.devenv:org.spoofax.jsglr:$spoofax2DevenvVersion")
    api("org.metaborg.devenv:sdf2table:$spoofax2DevenvVersion")
    api("org.metaborg.devenv:sdf2parenthesize:$spoofax2DevenvVersion")
    api("org.metaborg.devenv:org.metaborg.parsetable:$spoofax2DevenvVersion")
    api("org.metaborg.devenv:stratego.build:$spoofax2DevenvVersion")
    api("org.metaborg.devenv:nabl2.terms:$spoofax2DevenvVersion")
    api("org.metaborg.devenv:nabl2.solver:$spoofax2DevenvVersion")
    api("org.metaborg.devenv:statix.solver:$spoofax2DevenvVersion")

    /// Picocli
    api("info.picocli:picocli:$picocliVersion")
    api("info.picocli:picocli-codegen:$picocliVersion")

    // Annotation-only dependencies
    /// javax.inject
    api("javax.inject:javax.inject:$javaxInjectVersion")
    /// Checkerframework
    api("org.checkerframework:checker-qual-android:$checkerframeworkVersion") // Use android version: annotation retention policy is class instead of runtime.
    /// FindBugs JSR305
    api("com.google.code.findbugs:jsr305:3.0.2")

    // Annotation processor dependencies
    /// Dagger
    api("com.google.dagger:dagger:$daggerVersion")
    api("com.google.dagger:dagger-compiler:$daggerVersion")
    /// Derive4j
    api("org.derive4j:derive4j:$derive4jVersion")
    api("org.derive4j:derive4j-annotation:$derive4jVersion")
    /// org.immutables
    api("org.immutables:value:$immutablesVersion")
    api("org.immutables:value-annotations:$immutablesVersion")
    // Yaml
    api("org.yaml:snakeyaml:$yamlVersion")

    // Gradle plugins
    api("org.metaborg.devenv:spoofax.gradle:$spoofax2DevenvVersion")


    // Test dependencies // TODO: should be in a separate platform?
    api("org.junit.jupiter:junit-jupiter-api:5.3.1")
    api("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    api("com.google.jimfs:jimfs:1.1")
    api("nl.jqno.equalsverifier:equalsverifier:3.1.12")
  }
}

publishing {
  publications {
    create<MavenPublication>("JavaPlatform") {
      from(components["javaPlatform"])
    }
  }
}
