package com.yodle.griddle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class ScroogeGradlePluginApplyTest extends Specification {

  Project project

  def setup() {
    project = ProjectBuilder.builder().build()
  }

  void "applying scrooge plugin adds plugin"() {
    when:
      project.apply plugin: 'scrooge'

    then:
      project.plugins.hasPlugin(ScroogeScalaPlugin)
  }

}
