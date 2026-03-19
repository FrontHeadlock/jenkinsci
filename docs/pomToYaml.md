# 루트의 pom.xml을 가독성 고려한 yaml 형태로 수정
project:
modelVersion: "4.0.0"

parent:
groupId: org.jenkins-ci
artifactId: jenkins
version: 2.1328.vfd10e2339a_19

coordinates:
groupId: org.jenkins-ci.main
artifactId: jenkins-parent
version: "${revision}${changelist}"
packaging: pom

metadata:
name: Jenkins main module
description: Main module that builds jenkins.war
url: https://github.com/jenkinsci/jenkins

licenses:
- name: MIT License
url: https://opensource.org/licenses/MIT

modules:
- bom
- websocket/spi
- websocket/jetty12-ee9
- core
- war
- test
- cli
- coverage

scm:
connection: scm:git:https://github.com/jenkinsci/jenkins.git
developerConnection: scm:git:git@github.com:jenkinsci/jenkins.git
tag: "${scmTag}"
url: https://github.com/jenkinsci/jenkins

issueManagement:
system: GitHub
url: https://github.com/jenkinsci/jenkins/issues

properties:
revision: "2.556"
changelist: "-SNAPSHOT"
buildTimestamp: "2026-03-17T10:52:54Z"

    remotingVersion: 3355.v388858a_47b_33
    winstioneVersion: 8.1034.v0b_9170f90e17
    nodeVersion: 24.14.0

    spotbugs:
      effort: Max
      threshold: Medium

    versions:
      accessModifier: 1.35
      antlr: 4.13.2
      bridgeMethodInjector: 1.32

repositories:
- id: repo.jenkins-ci.org
url: https://repo.jenkins-ci.org/public/

pluginRepositories:
- id: repo.jenkins-ci.org
url: https://repo.jenkins-ci.org/public/

build:
defaultGoal: install

    resources:
      - directory: src/main/resources
        filtering: false
      - directory: src/filter/resources
        filtering: true

    pluginManagement:
      mavenCompiler:
        args:
          - "-Xpkginfo:always"

      mavenJavadoc:
        quiet: true
        splitIndex: true

      mavenSurefire:
        systemProperties:
          tmpDir: "${project.build.directory}"
          timeout: 3600
          headless: true

      jarsigner:
        version: 3.1.0
        config:
          alias: "${hudson.sign.alias}"
          keystore: "${hudson.sign.keystore}"

      spotbugs:
        maxHeap: 768

      checkstyle:
        version: 13.3.0
        configLocation: src/checkstyle/checkstyle-configuration.xml

      jacoco:
        version: 0.8.14

    plugins:
      spotless:
        java:
          removeUnusedImports: true
          trimTrailingWhitespace: true

      release:
        skipTests: true
        skipSpotbugs: true

      enforcer:
        bannedDependencies:
          - commons-logging
          - sisu-guice

profiles:
debug:
activeByDefault: true
signing:
alias: jenkins
keystore: ../dummy.keystore
password: jenkins

    release:
      gpgSigning: true

    lts-release:
      changelogUrl: https://www.jenkins.io/changelog-stable

    yarn:
      enabledIf: package.json exists
      steps:
        - install node
        - yarn install
        - yarn build

    yarn-ci-lint:
      condition: env.CI=true
      steps:
        - yarn lint:ci
        - prettier check

    yarn-lint:
      condition: env.CI=false
      steps:
        - yarn lint