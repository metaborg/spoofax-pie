#!groovy
@Library('metaborg.jenkins.pipeline@develop') _

gradlePipeline(
  publish: false,
  upstreamProjects: [
    '/metaborg/coronium/develop',
    '/metaborg/spoofax.gradle/develop',
    '/metaborg/log/develop',
    '/metaborg/resource/develop',
    '/metaborg/pie/develop',
    '/metaborg/spoofax-releng/master'
  ],
  slack: true,
  slackChannel: "#pie-dev"
)
