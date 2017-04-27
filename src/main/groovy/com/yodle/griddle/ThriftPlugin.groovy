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
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.plugins.ide.idea.IdeaPlugin

class ThriftPlugin extends GeneratingPlugin {

  void apply(Project project) {
    super.apply(project)
  }

  @Override protected getMainSourceSet(Project project) {
    return project.sourceSets.main.java;
  }

  @Override protected Task createGenerateInterfacesTask(Project project) {
    def generateInterfacesTask = project.tasks.create(GENERATE_INTERFACES_TASK_NAME, GenerateInterfacesThriftTask.class, new Action<GenerateInterfacesThriftTask>() {
      @Override void execute(GenerateInterfacesThriftTask t)
      {
        t.inputFiles = project.fileTree((Object){project.thriftSrcDir}).matching {
          include '**/*.thrift'
        }
        t.dependencyFiles = project.fileTree((Object){project.dependencyIdlDir})
        t.includedFiles = project.fileTree((Object){project.includedIdlDir})
        t.outputDirs = project.files((Object){project.thriftGenDir})
        t.setLanguage('java:hashcode')
      }
    })

    generateInterfacesTask.configure()

    return generateInterfacesTask
  }

  @Override protected String getAdjustedThriftGenDir(Project project, String language)
  {
    String outDir = "/gen-java";
    if (language.contains("beans")) {
      outDir = "/gen-javabean";
    }
    return project.thriftGenDir + outDir;
  }
}
