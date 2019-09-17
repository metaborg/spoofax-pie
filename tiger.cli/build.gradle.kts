plugins {
  id("org.metaborg.gradle.config.java-application")
  id("com.palantir.graal") version "0.4.0"
}

application {
  mainClassName = "mb.tiger.cli.Main"
}

graal {
  mainClass("mb.tiger.cli.Main")
  outputName("tiger")
  option("--no-fallback")
  option("--report-unsupported-elements-at-runtime")
  option("--initialize-at-build-time=mb,com,org.metaborg.util.log,org.slf4j,dagger,dagger.internal.InstanceFactory,picocli")
  option("-H:+ReportExceptionStackTraces")
  option("-H:IncludeResources=mb/tiger/target/.*") /* https://github.com/oracle/graal/blob/master/substratevm/RESOURCES.md */
  option("-H:DynamicProxyConfigurationFiles=proxy.json") /* https://github.com/oracle/graal/blob/master/substratevm/DYNAMIC_PROXY.md */
  option("-H:ReflectionConfigurationFiles=reflection.json") /* https://github.com/oracle/graal/blob/master/substratevm/REFLECTION.md */
}

dependencies {
  implementation(platform(project(":depconstraints")))

  implementation(project(":tiger.spoofax"))
  implementation(project(":spoofax.cli"))
  implementation("org.metaborg:log.backend.slf4j")
  implementation("org.metaborg:pie.runtime")
  implementation("org.metaborg:pie.dagger")

  implementation("org.slf4j:slf4j-simple:1.7.26")

  compileOnly("org.checkerframework:checker-qual-android")
}


