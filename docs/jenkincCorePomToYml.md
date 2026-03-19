project:
modelVersion: "4.0.0"

parent:
groupId: org.jenkins-ci.main
artifactId: jenkins-parent
version: "${revision}${changelist}"

artifactId: jenkins-core
name: Jenkins core
description: Jenkins core code and view files to render HTML.
url: https://github.com/jenkinsci/jenkins

properties:
remoting.minimum.supported.version: 3176.v207ec082a_8c0
jacocoSurefireArgs: ""

dependencyManagement:
dependencies:
- groupId: org.jenkins-ci.main
artifactId: jenkins-bom
version: "${project.version}"
type: pom
scope: import

      - groupId: org.xmlunit
        artifactId: xmlunit-bom
        version: 2.11.0
        type: pom
        scope: import

dependencies:
- groupId: "${project.groupId}"
artifactId: cli
version: "${project.version}"

    - groupId: "${project.groupId}"
      artifactId: remoting

    - groupId: args4j
      artifactId: args4j

    - groupId: com.github.spotbugs
      artifactId: spotbugs-annotations
      exclusions:
        - groupId: com.google.code.findbugs
          artifactId: jsr305

    - groupId: com.google.guava
      artifactId: guava
      exclusions:
        - groupId: com.google.errorprone
          artifactId: error_prone_annotations
        - groupId: com.google.j2objc
          artifactId: j2objc-annotations
        - groupId: org.checkerframework
          artifactId: checker-qual

    - groupId: com.google.inject
      artifactId: guice
      exclusions:
        - groupId: aopalliance
          artifactId: aopalliance

    - groupId: com.sun.xml.txw2
      artifactId: txw2
      exclusions:
        - groupId: javax.xml.stream
          artifactId: stax-api

    - groupId: com.thoughtworks.xstream
      artifactId: xstream
      exclusions:
        - groupId: xmlpull
          artifactId: xmlpull
        - groupId: xpp3
          artifactId: xpp3_min

    - groupId: commons-beanutils
      artifactId: commons-beanutils

    - groupId: commons-codec
      artifactId: commons-codec

    - groupId: commons-collections
      artifactId: commons-collections

    - groupId: commons-io
      artifactId: commons-io

    - groupId: commons-lang
      artifactId: commons-lang

    - groupId: io.jenkins.tools
      artifactId: bridge-method-annotation
      version: "${bridge-method-injector.version}"

    - groupId: jakarta.servlet.jsp.jstl
      artifactId: jakarta.servlet.jsp.jstl-api
      exclusions:
        - groupId: jakarta.el
          artifactId: jakarta.el-api
        - groupId: jakarta.servlet
          artifactId: jakarta.servlet-api
        - groupId: jakarta.xml.bind
          artifactId: jakarta.xml.bind-api

    - groupId: jaxen
      artifactId: jaxen
      exclusions:
        - groupId: dom4j
          artifactId: dom4j
        - groupId: jdom
          artifactId: jdom
        - groupId: xerces
          artifactId: xercesImpl
        - groupId: xml-apis
          artifactId: xml-apis
        - groupId: xom
          artifactId: xom

    - groupId: jline
      artifactId: jline
      version: 2.14.6
      scope: compile

    - groupId: net.java.dev.jna
      artifactId: jna

    - groupId: net.java.sezpoz
      artifactId: sezpoz

    - groupId: net.jcip
      artifactId: jcip-annotations

    - groupId: org.antlr
      artifactId: antlr4-runtime

    - groupId: org.apache.ant
      artifactId: ant

    - groupId: org.apache.commons
      artifactId: commons-fileupload2-core

    - groupId: org.apache.commons
      artifactId: commons-fileupload2-jakarta-servlet5

    - groupId: org.codehaus.groovy
      artifactId: groovy-all

    - groupId: org.fusesource.jansi
      artifactId: jansi

    - groupId: org.jenkins-ci
      artifactId: annotation-indexer

    - groupId: org.jenkins-ci
      artifactId: commons-jelly-tags-fmt

    - groupId: org.jenkins-ci
      artifactId: commons-jelly-tags-xml
      exclusions:
        - groupId: jaxen
          artifactId: jaxen
        - groupId: org.dom4j
          artifactId: dom4j

    - groupId: org.jenkins-ci
      artifactId: commons-jexl

    - groupId: org.jenkins-ci
      artifactId: crypto-util

    - groupId: org.jenkins-ci
      artifactId: memory-monitor

    - groupId: org.jenkins-ci
      artifactId: symbol-annotation

    - groupId: org.jenkins-ci
      artifactId: task-reactor

    - groupId: org.jenkins-ci
      artifactId: version-number

    - groupId: org.jenkins-ci.main
      artifactId: websocket-spi
      version: "${project.version}"
      exclusions:
        - groupId: jakarta.servlet
          artifactId: jakarta.servlet-api

    - groupId: org.jfree
      artifactId: jfreechart

    - groupId: org.jvnet.hudson
      artifactId: commons-jelly-tags-define

    - groupId: org.jvnet.localizer
      artifactId: localizer

    - groupId: org.jvnet.winp
      artifactId: winp

    - groupId: org.kohsuke
      artifactId: access-modifier-annotation

    - groupId: org.kohsuke
      artifactId: windows-package-checker

    - groupId: org.kohsuke.metainf-services
      artifactId: metainf-services
      optional: true

    - groupId: org.kohsuke.stapler
      artifactId: json-lib

    - groupId: org.kohsuke.stapler
      artifactId: stapler

    - groupId: org.kohsuke.stapler
      artifactId: stapler-adjunct-codemirror

    - groupId: org.kohsuke.stapler
      artifactId: stapler-groovy
      exclusions:
        - groupId: commons-jelly
          artifactId: commons-jelly
        - groupId: commons-jexl
          artifactId: commons-jexl
        - groupId: org.codehaus.groovy
          artifactId: groovy
        - groupId: org.jvnet.hudson
          artifactId: commons-jexl

    - groupId: org.slf4j
      artifactId: jcl-over-slf4j

    - groupId: org.slf4j
      artifactId: log4j-over-slf4j

    - groupId: org.springframework.security
      artifactId: spring-security-web
      exclusions:
        - groupId: io.micrometer
          artifactId: micrometer-observation
        - groupId: org.springframework
          artifactId: spring-jcl

    - groupId: xpp3
      artifactId: xpp3
      version: 1.1.4c

    - groupId: jakarta.servlet
      artifactId: jakarta.servlet-api
      scope: provided

    - groupId: org.jenkins-ci
      artifactId: core-annotation-processors
      version: 59.ve181f58722c6
      scope: provided
      optional: true

    - groupId: org.hamcrest
      artifactId: hamcrest
      scope: test

    - groupId: org.jenkins-ci
      artifactId: test-annotations
      scope: test

    - groupId: org.junit.jupiter
      artifactId: junit-jupiter
      scope: test

    - groupId: org.mockito
      artifactId: mockito-junit-jupiter
      scope: test

    - groupId: org.slf4j
      artifactId: slf4j-jdk14
      scope: test

    - groupId: org.xmlunit
      artifactId: xmlunit-core
      scope: test

    - groupId: org.xmlunit
      artifactId: xmlunit-matchers
      scope: test

build:
plugins:
- groupId: org.apache.maven.plugins
artifactId: maven-enforcer-plugin
executions:
- id: enforce-banned-dependencies
goals:
- enforce
configuration:
rules:
bannedDependencies:
excludes:
- com.fasterxml.jackson.*
- com.github.ben-manes.caffeine:caffeine
- com.github.jnr:jnr-posix
- com.github.mwiede:jsch
- com.google.code.gson:gson
- com.jayway.jsonpath:json-path
- commons-httpclient:commons-httpclient
- com.sun.activation:javax.activation
- com.sun.mail:javax.mail
- com.sun.xml.bind:jaxb-impl
- io.jsonwebtoken
- jakarta.activation:jakarta.activation-api:*:jar:compile
- jakarta.activation:jakarta.activation-api:*:jar:runtime
- jakarta.mail:jakarta.mail-api
- javax.activation:javax.activation-api
- javax.mail:javax.mail-api
- javax.xml.bind:jaxb-api
- joda-time:joda-time
- net.bytebuddy:byte-buddy:*:jar:compile
- net.bytebuddy:byte-buddy:*:jar:runtime
- net.i2p.crypto:eddsa
- net.minidev
- org.apache.commons:commons-lang3
- org.apache.commons:commons-text
- org.apache.httpcomponents
- org.bouncycastle
- org.eclipse.angus:angus-activation
- org.eclipse.angus:angus-mail
- org.glassfish.jersey.*
- org.json:json
- org.ow2.asm
- org.yaml:snakeyaml

      - groupId: org.codehaus.mojo
        artifactId: build-helper-maven-plugin
        executions:
          - id: add-source
            goals:
              - add-source
            phase: generate-sources
            configuration:
              sources:
                - "${project.build.directory}/generated-sources/antlr4"
                - "${project.build.directory}/generated-sources/localizer"
                - "${project.build.directory}/generated-sources/taglib-interface"

      - groupId: org.jenkins-ci.tools
        artifactId: maven-hpi-plugin
        executions:
          - goals:
              - generate-taglib-interface
              - record-core-location

      - groupId: io.jenkins.tools
        artifactId: bridge-method-injector
        executions:
          - goals:
              - process

      - groupId: org.jvnet.localizer
        artifactId: localizer-maven-plugin
        executions:
          - goals:
              - generate
            configuration:
              fileMask: Messages.properties
              outputDirectory: target/generated-sources/localizer
              accessModifierAnnotations: true

      - groupId: org.kohsuke
        artifactId: access-modifier-checker
        executions:
          - goals:
              - enforce

      - groupId: org.antlr
        artifactId: antlr4-maven-plugin
        executions:
          - id: antlr
            goals:
              - antlr4

      - artifactId: maven-dependency-plugin
        executions:
          - goals:
              - properties
          - id: winsw
            goals:
              - copy
            phase: generate-resources
            configuration:
              artifactItems:
                - groupId: com.sun.winsw
                  artifactId: winsw
                  version: 2.9.0
                  classifier: net4
                  type: exe
                  outputDirectory: "${project.build.outputDirectory}/windows-service"
                  destFileName: jenkins.exe

      - artifactId: maven-surefire-plugin
        configuration:
          argLine: '@{jacocoSurefireArgs} --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED -javaagent:"${org.mockito:mockito-core:jar}"'
          reuseForks: false

      - artifactId: maven-jar-plugin
        configuration:
          archive:
            manifest:
              mainClass: hudson.Main
              addClasspath: true
              classpathPrefix: ""

reporting:
plugins:
- groupId: io.jenkins.tools.maven
artifactId: stapler-maven-plugin
configuration:
patterns:
- /lib/.*

      - artifactId: maven-project-info-reports-plugin
        configuration:
          dependencyLocationsEnabled: false

profiles:
- id: debug
activation:
activeByDefault: true

    - id: enable-jacoco
      build:
        plugins:
          - groupId: org.jacoco
            artifactId: jacoco-maven-plugin
            executions:
              - goals:
                  - prepare-agent
                configuration:
                  propertyName: jacocoSurefireArgs
                  includes:
                    - hudson/*
                    - hudson/**/*
                    - jenkins/*
                    - jenkins/**/*
                    - org/jenkins/**/*
                  excludes:
                    - "**/Messages.class"