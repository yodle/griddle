package com.yodle.griddle

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task

abstract class ScroogePlugin extends GeneratingPlugin {

  public static final String SCROOGE_GEN_CONFIGURATION = 'scroogeGen'
  private static final String GENERATE_INTERFACES_TASK_NAME = 'generateInterfaces'

  @Override protected Task createGenerateInterfacesTask(Project project)
  {
    def generateInterfacesTask = project.tasks.create(GENERATE_INTERFACES_TASK_NAME, GenerateInterfacesScroogeTask.class, new Action<GenerateInterfacesScroogeTask>() {
      @Override void execute(GenerateInterfacesScroogeTask t)
      {
        t.inputFiles = project.fileTree((Object){project.thriftSrcDir}).matching {
          include '**/*.thrift'
        }
        t.dependencyFiles = project.fileTree((Object){project.dependencyIdlDir})
        t.includedFiles = project.fileTree((Object){project.includedIdlDir})
        t.outputDirs = project.files((Object){project.thriftGenDir})
        t.setLanguage(getLanguage())
        t.setMain('com.twitter.scrooge.Main')
      }
    })

    generateInterfacesTask.configure()

    return generateInterfacesTask
  }

  void apply(Project project) {
    project.plugins.apply(getLanguage())
    project.configurations.create(SCROOGE_GEN_CONFIGURATION)
    super.apply(project)
  }

  abstract protected String getLanguage();
}
