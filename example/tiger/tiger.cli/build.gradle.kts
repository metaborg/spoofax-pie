plugins {
  id("org.metaborg.gradle.config.java-application")
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.cli")
//  application
}

//graal {
//  graalVersion("19.2.0.1")
//
//  mainClass("mb.tiger.cli.Main")
//  outputName("tiger")
//  option("--no-fallback")
//  option("--report-unsupported-elements-at-runtime")
//  option("--initialize-at-build-time=mb,com,org.metaborg.util.log,org.slf4j,dagger,dagger.internal.InstanceFactory,picocli")
//  option("-H:+ReportExceptionStackTraces")
//  option("-H:IncludeResources=mb/tiger/target/.*") /* https://github.com/oracle/graal/blob/master/substratevm/RESOURCES.md */
//  option("-H:DynamicProxyConfigurationFiles=src/main/gni/proxy.json") /* https://github.com/oracle/graal/blob/master/substratevm/DYNAMIC_PROXY.md */
//  option("-H:ReflectionConfigurationFiles=src/main/gni/reflection.json") /* https://github.com/oracle/graal/blob/master/substratevm/REFLECTION.md */
//}
