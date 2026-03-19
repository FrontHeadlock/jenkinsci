project:
modelVersion: "4.0.0"

parent:
groupId: org.jenkins-ci.main
artifactId: jenkins-parent
version: "${revision}${changelist}"

artifactId: jenkins-war
packaging: war

metadata:
name: Jenkins war
description: >
Creates a war file. Also includes additional static web resources,
such as images, CSS, JavaScript, and some HTML files.
url: https://github.com/jenkinsci/jenkins

properties:
JENKINS_HOME: "${basedir}/work"
contextPath: /jenkins
host: localhost
port: 8080
minaSshdApiVersion: 2.16.0-167.va_269f38cc024
remoting.minimum.supported.version: 3176.v207ec082a_8c0

dependencyManagement:
dependencies:
- groupId: org.jenkins-ci.main
artifactId: jenkins-bom
version: "${project.version}"
type: pom
scope: import

dependencies:
- groupId: "${project.groupId}"
artifactId: cli
version: "${project.version}"

    - groupId: "${project.groupId}"
      artifactId: remoting

    - groupId: org.jenkins-ci.main
      artifactId: jenkins-core
      version: "${project.version}"

    - groupId: org.jenkins-ci.main
      artifactId: websocket-jetty12-ee9
      version: "${project.version}"

    - groupId: org.slf4j
      artifactId: slf4j-jdk14

    - groupId: org.jenkins-ci
      artifactId: winstone
      version: "${winstone.version}"
      scope: test

    - groupId: org.junit.jupiter
      artifactId: junit-jupiter
      scope: test

build:
finalName: jenkins

    plugins:
      - artifactId: maven-enforcer-plugin
        executions:
          - id: display-info
            rules:
              requireJavaVersion:
                version: "11"
              enforceBytecodeVersion:
                maxJdkVersion: "11"
                excludes:
                  - io.jenkins.tools:bridge-method-annotation
                  - org.jenkins-ci:annotation-indexer
                  - org.jenkins-ci:commons-jelly
                  - org.jenkins-ci:commons-jelly-tags-fmt
                  - org.jenkins-ci:commons-jelly-tags-xml
                  - org.jenkins-ci:crypto-util
                  - org.jenkins-ci.main:cli
                  - org.jenkins-ci.main:jenkins-core
                  - org.jenkins-ci.main:remoting
                  - org.jenkins-ci.main:websocket-jetty12-ee9
                  - org.jenkins-ci.main:websocket-spi
                  - org.jenkins-ci:memory-monitor
                  - org.jenkins-ci:symbol-annotation
                  - org.jenkins-ci:task-reactor
                  - org.jenkins-ci:version-number
                  - org.jvnet.hudson:commons-jelly-tags-define
                  - org.jvnet.winp:winp
                  - org.kohsuke:access-modifier-annotation
                  - org.kohsuke.stapler:json-lib
                  - org.kohsuke.stapler:stapler
                  - org.kohsuke.stapler:stapler-groovy
                  - org.kohsuke.stapler:stapler-jelly
                  - org.springframework.security:spring-security-core
                  - org.springframework.security:spring-security-crypto
                  - org.springframework.security:spring-security-web
                  - org.springframework:spring-aop
                  - org.springframework:spring-beans
                  - org.springframework:spring-context
                  - org.springframework:spring-core
                  - org.springframework:spring-expression
                  - org.springframework:spring-web

      - artifactId: maven-compiler-plugin
        configuration:
          release: 11
          testRelease: 11

      - artifactId: maven-javadoc-plugin
        configuration:
          release: 11

      - artifactId: maven-war-plugin
        configuration:
          filteringDeploymentDescriptors: true
          archive:
            manifest:
              mainClass: executable.Main
            manifestEntries:
              Add-Opens: "java.base/java.lang java.base/java.io java.base/java.util"
              Implementation-Version: "${project.version}"
              Hudson-Version: "1.395"
              Jenkins-Version: "${project.version}"
              Remoting-Embedded-Version: "${remoting.version}"
              Remoting-Minimum-Supported-Version: "${remoting.minimum.supported.version}"

      - artifactId: maven-dependency-plugin
        executions:
          - id: list-dependencies
            goal: list
            phase: generate-resources
            configuration:
              outputFile: "${project.build.outputDirectory}/dependencies.txt"

          - id: resgen
            goal: copy
            phase: generate-resources
            configuration:
              artifactItems:
                - groupId: org.jenkins-ci
                  artifactId: winstone
                  outputDirectory: "${project.build.directory}/${project.build.finalName}/executable"
                  destFileName: winstone.jar
              outputDirectory: "${project.build.directory}/${project.build.finalName}/WEB-INF/plugins"
              stripVersion: true

          - id: detached-plugins
            goal: copy
            phase: generate-resources
            configuration:
              outputDirectory: "${project.build.directory}/${project.build.finalName}/WEB-INF/detached-plugins"
              stripVersion: true
              artifactItems:
                - artifactId: display-url-api
                  groupId: org.jenkins-ci.plugins
                  version: 2.217.va_6b_de84cc74b_
                  type: hpi
                - artifactId: mailer
                  groupId: org.jenkins-ci.plugins
                  version: 525.v2458b_d8a_1a_71
                  type: hpi
                - artifactId: matrix-auth
                  groupId: org.jenkins-ci.plugins
                  version: 3.2.9
                  type: hpi
                - artifactId: antisamy-markup-formatter
                  groupId: org.jenkins-ci.plugins
                  version: 173.v680e3a_b_69ff3
                  type: hpi
                - artifactId: matrix-project
                  groupId: org.jenkins-ci.plugins
                  version: 870.v9db_fcfc2f45b_
                  type: hpi
                - artifactId: script-security
                  groupId: org.jenkins-ci.plugins
                  version: 1399.ve6a_66547f6e1
                  type: hpi
                - artifactId: junit
                  groupId: org.jenkins-ci.plugins
                  version: 1403.vd9d1413fd205
                  type: hpi
                - artifactId: workflow-api
                  groupId: org.jenkins-ci.plugins.workflow
                  version: 1398.v67030756d3fb_
                  type: hpi
                - artifactId: plugin-util-api
                  groupId: io.jenkins.plugins
                  version: 6.1192.v30fe6e2837ff
                  type: hpi
                - artifactId: bootstrap5-api
                  groupId: io.jenkins.plugins
                  version: 5.3.8-895.v4d0d8e47fea_d
                  type: hpi
                - artifactId: checks-api
                  groupId: io.jenkins.plugins
                  version: 402.vca_263b_f200e3
                  type: hpi
                - artifactId: prism-api
                  groupId: io.jenkins.plugins
                  version: 1.30.0-703.v116fb_3b_5b_b_a_a_
                  type: hpi
                - artifactId: workflow-support
                  groupId: org.jenkins-ci.plugins.workflow
                  version: 1015.v785e5a_b_b_8b_22
                  type: hpi
                - artifactId: jackson2-api
                  groupId: org.jenkins-ci.plugins
                  version: 2.21.1-428.vf8dd988fa_d8d
                  type: hpi
                - artifactId: jackson3-api
                  groupId: io.jenkins.plugins
                  version: 3.1.0-64.v37e742c35905
                  type: hpi
                - artifactId: snakeyaml-engine-api
                  groupId: io.jenkins.plugins
                  version: 3.0.1-5.vd98ea_ff3b_92e
                  type: hpi
                - artifactId: echarts-api
                  groupId: io.jenkins.plugins
                  version: 6.0.0-1165.vd1283a_3e37d4
                  type: hpi
                - artifactId: eddsa-api
                  groupId: io.jenkins.plugins
                  version: 0.3.0.1-29.v67e9a_1c969b_b_
                  type: hpi
                - artifactId: snakeyaml-api
                  groupId: io.jenkins.plugins
                  version: 2.5-149.v72471e9c6371
                  type: hpi
                - artifactId: jakarta-xml-bind-api
                  groupId: io.jenkins.plugins
                  version: 4.0.6-12.vb_1833c1231d3
                  type: hpi
                - artifactId: caffeine-api
                  groupId: io.jenkins.plugins
                  version: 3.2.3-194.v31a_b_f7a_b_5a_81
                  type: hpi
                - artifactId: jquery3-api
                  groupId: io.jenkins.plugins
                  version: 3.7.1-619.vdb_10e002501a_
                  type: hpi
                - artifactId: font-awesome-api
                  groupId: io.jenkins.plugins
                  version: 7.2.0-965.ve3840b_696418
                  type: hpi
                - artifactId: workflow-step-api
                  groupId: org.jenkins-ci.plugins.workflow
                  version: 710.v3e456cc85233
                  type: hpi
                - artifactId: scm-api
                  groupId: org.jenkins-ci.plugins
                  version: 728.vc30dcf7a_0df5
                  type: hpi
                - artifactId: structs
                  groupId: org.jenkins-ci.plugins
                  version: 362.va_b_695ef4fdf9
                  type: hpi
                - artifactId: bouncycastle-api
                  groupId: org.jenkins-ci.plugins
                  version: 2.30.1.83-289.v8426fcd19371
                  type: hpi
                - artifactId: command-launcher
                  groupId: org.jenkins-ci.plugins
                  version: 123.v37cfdc92ef67
                  type: hpi
                - artifactId: jdk-tool
                  groupId: org.jenkins-ci.plugins
                  version: 83.v417146707a_3d
                  type: hpi
                - artifactId: jaxb
                  groupId: io.jenkins.plugins
                  version: 2.3.9-143.v5979df3304e6
                  type: hpi
                - artifactId: sshd
                  groupId: org.jenkins-ci.modules
                  version: 3.384.vc89b_5e138cf9
                  type: hpi
                - artifactId: trilead-api
                  groupId: org.jenkins-ci.plugins
                  version: 2.284.v1974ea_324382
                  type: hpi
                - artifactId: javax-activation-api
                  groupId: io.jenkins.plugins
                  version: 1.2.0-8
                  type: hpi
                - artifactId: javax-mail-api
                  groupId: io.jenkins.plugins
                  version: 1.6.2-11
                  type: hpi
                - artifactId: instance-identity
                  groupId: org.jenkins-ci.modules
                  version: 203.v15e81a_1b_7a_38
                  type: hpi
                - artifactId: apache-httpcomponents-client-4-api
                  groupId: org.jenkins-ci.plugins
                  version: 4.5.14-269.vfa_2321039a_83
                  type: hpi
                - artifactId: asm-api
                  groupId: io.jenkins.plugins
                  version: 9.9.1-189.vb_5ef2964da_91
                  type: hpi
                - artifactId: commons-lang3-api
                  groupId: io.jenkins.plugins
                  version: 3.20.0-109.ve43756e2d2b_4
                  type: hpi
                - artifactId: commons-text-api
                  groupId: io.jenkins.plugins
                  version: 1.15.0-218.va_61573470393
                  type: hpi
                - artifactId: gson-api
                  groupId: io.jenkins.plugins
                  version: 2.13.2-173.va_a_092315913c
                  type: hpi
                - artifactId: ionicons-api
                  groupId: io.jenkins.plugins
                  version: 94.vcc3065403257
                  type: hpi
                - artifactId: jakarta-activation-api
                  groupId: io.jenkins.plugins
                  version: 2.1.4-1
                  type: hpi
                - artifactId: jakarta-mail-api
                  groupId: io.jenkins.plugins
                  version: 2.1.5-1
                  type: hpi
                - artifactId: json-api
                  groupId: io.jenkins.plugins
                  version: 20250517-173.v596efb_962a_31
                  type: hpi
                - artifactId: mina-sshd-api-common
                  groupId: io.jenkins.plugins.mina-sshd-api
                  version: "${minaSshdApiVersion}"
                  type: hpi
                - artifactId: mina-sshd-api-core
                  groupId: io.jenkins.plugins.mina-sshd-api
                  version: "${minaSshdApiVersion}"
                  type: hpi

          - id: support-log-formatter
            goal: copy
            phase: generate-resources
            configuration:
              artifactItems:
                - groupId: io.jenkins.lib
                  artifactId: support-log-formatter
                  version: 123.v72513b_f61500
              outputDirectory: "${project.build.directory}"

      - artifactId: maven-antrun-plugin
        executions:
          - id: classes-copy
            goal: run
            phase: prepare-package
            configuration:
              move:
                from: "${project.build.directory}/classes/executable/**/*.class"
                to: "${project.build.directory}/${project.build.finalName}"

      - groupId: io.jenkins.tools.maven
        artifactId: license-maven-plugin
        configuration:
          generateLicenseXml: "${project.build.outputDirectory}/META-INF/licenses.xml"
          generateLicenseHtml: "${project.build.outputDirectory}/META-INF/licenses.html"
          attach: true

      - groupId: org.eclipse.jetty.ee9
        artifactId: jetty-ee9-maven-plugin
        version: 12.1.7
        configuration:
          scan: 0
          httpConnector:
            host: "${host}"
            port: "${port}"
          systemProperties:
            JENKINS_HOME: "${JENKINS_HOME}"
            stapler.jelly.noCache: true
            stapler.trace: true
            jenkins.model.Jenkins.SHOW_STACK_TRACE: true
            hudson.script.noCache: true
            stapler.resourcePath: "../core/src/main/resources;"
            hudson.bundled.plugins: "${project.build.directory}/${project.build.finalName}/WEB-INF/plugins/*.hpi"
            hudson.model.UsageStatistics.disabled: true
            hudson.Main.development: true
            javax.xml.transform.TransformerFactory: "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl"
          webApp:
            extraClasspath: "${project.basedir}/../core/src/main/resources,${project.build.directory}/support-log-formatter.jar"
            contextPath: "${contextPath}"
            webInfIncludeJarPattern: ".*(jenkins-core|target/classes).*"

profiles:
- id: sign
build:
plugins:
- artifactId: maven-jarsigner-plugin
executions:
- id: signWar
goal: sign
phase: pre-integration-test
configuration:
archive: "${project.build.directory}/${project.build.finalName}.war"
arguments:
- -tsa
- http://timestamp.digicert.com