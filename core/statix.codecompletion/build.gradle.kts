plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.gradle.config.junit-testing")
    jacoco
}

dependencies {
    api(platform(project(":spoofax.depconstraints")))
    annotationProcessor(platform(project(":spoofax.depconstraints")))
    testAnnotationProcessor(platform(project(":spoofax.depconstraints")))

    api(libs.metaborg.common)
    api(libs.spoofax3.statix.common)
    api(libs.spoofax3.tego.runtime)
    api(libs.metaborg.log.api)

    api(libs.statix.solver)
    api(libs.statix.generator)

    implementation(project(":stratego.common"))
    implementation(project(":jsglr.common"))

    compileOnly(libs.checkerframework.android)

    annotationProcessor(libs.immutables.value)
    testAnnotationProcessor(libs.immutables.value)

    testCompileOnly(libs.checkerframework.android)
    testImplementation(libs.equalsverifier)
    testImplementation(libs.metaborg.log.backend.slf4j)
    testImplementation(libs.slf4j.simple)
    testCompileOnly(libs.immutables.value)

    testImplementation(libs.opencsv)

    // Immutables
    testCompileOnly(libs.immutables.value)
    testAnnotationProcessor(libs.immutables.value)
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}
tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

//tasks { withType<Test> {
//  debug = true
//  maxHeapSize = "3g"
//} }
