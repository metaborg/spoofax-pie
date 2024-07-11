plugins {
    `java-platform`
    `maven-publish`
}

val commonVersion = "0.11.0"
val logVersion = "0.5.5"
val slf4jVersion = "1.7.30"
val resourceVersion = "0.14.1"
val pieVersion = "0.21.0"

val spoofax2Version: String by ext
val spoofax2DevenvVersion: String by ext

val picocliVersion = "4.5.0"

val javaxInjectVersion = "1"
val checkerframeworkVersion = "3.16.0"

val daggerVersion = "2.36" // Do not upgrade, causes Gradle/Kotlin compatibility issues due to upgrade to Kotlin 1.5.
val derive4jVersion = "1.1.1"
val immutablesVersion = "2.10.1"

val yamlVersion = "1.26"

dependencies {
    constraints {
        // Own projects
        api(project(":spoofax.common"))
        api(project(":aterm.common"))
        api(project(":jsglr.common"))
        api(project(":jsglr1.common"))
        api(project(":jsglr.pie"))
        api(project(":jsglr2.common"))
        api(project(":esv.common"))
        api(project(":stratego.common"))
        api(project(":constraint.common"))
        api(project(":nabl2.common"))
        api(project(":statix.codecompletion"))
        api(project(":statix.codecompletion.pie"))
        api(project(":statix.common"))
        api(project(":statix.pie"))
        api(project(":statix.multilang"))
        api(project(":spoofax2.common"))
        api(project(":tego.runtime"))
        api(project(":tooling.eclipsebundle"))  // TODO: bundle versions are not picked up when consuming this platform?

        api(project(":spoofax.compiler.interfaces"))
        api(project(":spoofax.resource"))
        api(project(":spoofax.core"))
        api(project(":spoofax.cli"))
        api(project(":spoofax.intellij"))
        api(project(":spoofax.eclipse")) // TODO: bundle versions are not picked up when consuming this platform?
        api(project(":spoofax.compiler"))
        api(project(":spoofax.compiler.gradle"))
        api(project(":spoofax.compiler.eclipsebundle"))  // TODO: bundle versions are not picked up when consuming this platform?


        // Main dependencies
        /// Common
        api("org.metaborg:common:$commonVersion")
        /// Log
        api("org.metaborg:log.api:$logVersion")
        api("org.metaborg:log.backend.logback:$logVersion")
        api("org.metaborg:log.backend.slf4j:$logVersion")
        api("org.metaborg:log.dagger:$logVersion")
        /// SLF4j
        api("org.slf4j:slf4j-simple:$slf4jVersion")
        /// Resource
        api("org.metaborg:resource:$resourceVersion")
        api("org.metaborg:resource.dagger:$resourceVersion")
        /// PIE
        api("org.metaborg:pie.api:$pieVersion")
        api("org.metaborg:pie.graph:$pieVersion")
        api("org.metaborg:pie.runtime:$pieVersion")
        api("org.metaborg:pie.dagger:$pieVersion")
        api("org.metaborg:pie.task.java:$pieVersion")
        api("org.metaborg:pie.task.archive:$pieVersion")
        api("org.metaborg:pie.serde.fst:$pieVersion")
        /// Spoofax 2.x
        runtime("org.metaborg:strategoxt-min-jar:$spoofax2Version")
        /// Spoofax 2.x with devenv override
        api("org.metaborg.devenv:org.strategoxt.strj:$spoofax2DevenvVersion")
        api("org.metaborg.devenv:org.spoofax.terms:$spoofax2DevenvVersion")
        api("org.metaborg.devenv:org.metaborg.util:$spoofax2DevenvVersion")
        api("org.metaborg.devenv:org.spoofax.interpreter.core:$spoofax2DevenvVersion")
        api("org.metaborg.devenv:org.spoofax.jsglr:$spoofax2DevenvVersion")
        api("org.metaborg.devenv:org.spoofax.jsglr2:$spoofax2DevenvVersion")
        api("org.metaborg.devenv:jsglr.shared:$spoofax2DevenvVersion")
        api("org.metaborg.devenv:sdf2table:$spoofax2DevenvVersion")
        api("org.metaborg.devenv:sdf2parenthesize:$spoofax2DevenvVersion")
        api("org.metaborg.devenv:org.metaborg.parsetable:$spoofax2DevenvVersion")
        api("org.metaborg.devenv:stratego.build:$spoofax2DevenvVersion")
        api("org.metaborg.devenv:nabl2.terms:$spoofax2DevenvVersion")
        api("org.metaborg.devenv:nabl2.solver:$spoofax2DevenvVersion")
        api("org.metaborg.devenv:statix.solver:$spoofax2DevenvVersion")
        api("org.metaborg.devenv:statix.generator:$spoofax2DevenvVersion")

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
        api("org.immutables:serial:$immutablesVersion")
        api("org.immutables:value:$immutablesVersion")
        api("org.immutables:value-annotations:$immutablesVersion")
        // Yaml
        api("org.yaml:snakeyaml:$yamlVersion")

        // Gradle plugins
        api("org.metaborg.devenv:spoofax.gradle:$spoofax2DevenvVersion")


        // Test dependencies // TODO: should be in a separate platform?
        api("org.junit.jupiter:junit-jupiter-api:${metaborg.junitVersion}")
        api("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
        api(libs.jimfs)
        api("nl.jqno.equalsverifier:equalsverifier:3.16.1")
    }
}

publishing {
    publications {
        create<MavenPublication>("JavaPlatform") {
            from(components["javaPlatform"])
        }
    }
}
