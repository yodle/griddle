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

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.maven.Conf2ScopeMappingContainer
import org.gradle.api.plugins.MavenPlugin
import org.gradle.api.tasks.bundling.Jar

class IdlPlugin implements Plugin<Project> {
  public static final String IDL_CONFIGURATION = 'idl'
  public static final String COMPILED_IDL_CONFIGURATION = 'compiledIdl'
  public static final String COPY_DEPENDENCY_IDL_TASK_NAME = 'copyDependencyIdl'
  public static final String COPY_INCLUDED_IDL_TASK_NAME = 'copyIncludedIdl'

  @Override void apply(Project project) {
    project.plugins.apply('java')

    def idlConfiguration = project.configurations.create(IDL_CONFIGURATION)
    def compiledIdlConfiguration = project.configurations.create(COMPILED_IDL_CONFIGURATION)

    project.configurations.getByName('compile').extendsFrom project.configurations.getByName(COMPILED_IDL_CONFIGURATION)
    project.configurations.getByName('compile').extendsFrom project.configurations.getByName(IDL_CONFIGURATION)

    project.ext.set('thriftSrcDir',"${project.getProjectDir().getPath()}/src/main/thrift")
    project.ext.set('thriftGenDir', "${project.getProjectDir().getPath()}/build/gen-src")
    project.ext.set('dependencyIdlDir', "${project.getProjectDir().getPath()}/build/idl/dependency")
    project.ext.set('includedIdlDir', "${project.getProjectDir().getPath()}/build/idl/included")

    def copyDependencyIdlTask = project.tasks.create(COPY_DEPENDENCY_IDL_TASK_NAME, CopyIdlTask.class)
    copyDependencyIdlTask.inputFiles = project.configurations.getByName(IDL_CONFIGURATION)
    copyDependencyIdlTask.outputDirs = project.files(project.file((Object){project.dependencyIdlDir}))
    copyDependencyIdlTask.dependsOn project.configurations.getByName(IDL_CONFIGURATION)

    def copyIncludedIdlTask = project.tasks.create(COPY_INCLUDED_IDL_TASK_NAME, CopyIdlTask.class)
    copyIncludedIdlTask.inputFiles = project.configurations.getByName(COMPILED_IDL_CONFIGURATION)
    copyIncludedIdlTask.outputDirs = project.files(project.file((Object){project.includedIdlDir}))
    copyIncludedIdlTask.dependsOn project.configurations.getByName(COMPILED_IDL_CONFIGURATION)

    project.tasks.create('idlJar', Jar.class, new Action<Jar>(){
      @Override void execute(Jar t) {
        t.classifier = 'idl'
      }
    });
    project.tasks.getByName('idlJar').from {project.thriftSrcDir}
    project.tasks.getByName('jar').from {project.thriftSrcDir}

    project.artifacts.add(IDL_CONFIGURATION, project.tasks.getByName('idlJar'))

    project.tasks.getByName('assemble').dependsOn 'idlJar'


    project.plugins.withType(MavenPlugin) {
      project.conf2ScopeMappings.addMapping(1, idlConfiguration, Conf2ScopeMappingContainer.COMPILE)
      project.conf2ScopeMappings.addMapping(1, compiledIdlConfiguration, Conf2ScopeMappingContainer.COMPILE)
    }
  }
}
