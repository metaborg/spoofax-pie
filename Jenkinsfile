#!groovy
@Library('metaborg.jenkins.pipeline') _

gradlePipeline(
  buildOtherBranch: false,
  buildTag: false,
  gradleParallel: false,
  gradleMaxWorkers: '1',
  gradleArgs: '--scan',
  slack: true,
  slackChannel: '#spoofax3-dev'
)
