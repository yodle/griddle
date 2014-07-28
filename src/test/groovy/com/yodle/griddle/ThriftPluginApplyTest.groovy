/*
 * Copyright 2014 Yodle, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
