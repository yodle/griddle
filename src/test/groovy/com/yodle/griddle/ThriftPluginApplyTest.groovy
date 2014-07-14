package com.yodle.griddle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class ThriftPluginApplyTest extends Specification {

  Project project

  def setup() {
    project = ProjectBuilder.builder().build()
  }

  void "applying thrift plugin adds plugin"() {
    when:
      project.apply plugin: 'thrift'

    then:
      project.plugins.hasPlugin(ThriftPlugin)
  }

}
