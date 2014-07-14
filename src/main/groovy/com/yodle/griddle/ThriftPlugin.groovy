package com.yodle.griddle

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task

class ThriftPlugin extends GeneratingPlugin {
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
}
