package com.yodle.gradle

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import org.gradle.api.tasks.bundling.Jar

abstract class ScroogePlugin extends GeneratingPlugin {

  private static final String SCROOGE_GEN_CONFIGURATION = 'scroogeGen'
  private static final String GENERATE_INTERFACES_TASK_NAME = 'generateInterfaces'

  @Override protected Task createGenerateInterfacesTask(Project project)
  {
    def generateInterfacesTask = project.tasks.create(GENERATE_INTERFACES_TASK_NAME, GenerateInterfacesTask.class, new Action<GenerateInterfacesTask>() {
      @Override void execute(GenerateInterfacesTask t)
      {
        t.inputFiles = project.files(
                project.fileTree((Object){project.thriftSrcDir}),
                project.fileTree((Object){project.dependencyIdlDir})
        );
        t.includedFiles = project.fileTree((Object){project.includedIdlDir})
        t.outputDirs = project.files((Object){project.thriftGenDir})
        t.setMain('com.twitter.scrooge.Main')
      }
    })

    generateInterfacesTask.doFirst {
      if (useFinagle)
        args '--finagle'
      args (['-d', outputDirs.getSingleFile(), '-l', getLanguage()])
      args (['-i', project.fileTree((Object){project.dependencyIdlDir}).getDir()])
      args (['-i', includedFiles.dir])
      args inputFiles.files

      classpath project.configurations.getByName(SCROOGE_GEN_CONFIGURATION)
    }

    return generateInterfacesTask
  }

  void apply(Project project) {
    project.plugins.apply(getLanguage())
    project.configurations.create(SCROOGE_GEN_CONFIGURATION)
    super.apply(project)
  }

  abstract protected String getLanguage();
}
