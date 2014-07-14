package com.yodle.griddle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class IdlPluginApplyTest extends Specification {

  Project project

  def setup() {
    project = ProjectBuilder.builder().build()
  }

  void "applying idl plugin adds plugin"() {
    when:
      project.apply plugin: 'idl'

    then:
      project.plugins.hasPlugin(IdlPlugin)
  }

}
