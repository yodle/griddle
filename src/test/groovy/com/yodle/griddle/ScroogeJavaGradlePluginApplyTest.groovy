package com.yodle.griddle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class ScroogeJavaGradlePluginApplyTest extends Specification {

  Project project

  def setup() {
    project = ProjectBuilder.builder().build()
  }

  void "applying scrooge-java plugin adds plugin"() {
    when:
      project.apply plugin: 'scrooge-java'

    then:
      project.plugins.hasPlugin(ScroogeJavaPlugin)
  }

}
