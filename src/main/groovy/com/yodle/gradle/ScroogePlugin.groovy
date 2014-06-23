package com.yodle.gradle

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import org.gradle.api.tasks.bundling.Jar

abstract class ScroogePlugin implements Plugin<Project> {

  private static final String SCROOGE_GEN_CONFIGURATION = 'scroogeGen'
  private static final String GENERATE_INTERFACES_TASK_NAME = 'generateInterfaces'

  void apply(Project project) {
    project.plugins.apply(getLanguage())
    project.plugins.apply('idl')
    project.configurations.create(SCROOGE_GEN_CONFIGURATION)
    project.configurations.getByName('compile').extendsFrom project.configurations.getByName(IdlPlugin.COMPILED_IDL_CONFIGURATION)

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

    generateInterfacesTask.dependsOn project.tasks.getByName(IdlPlugin.COPY_DEPENDENCY_IDL_TASK_NAME)
    generateInterfacesTask.dependsOn project.tasks.getByName(IdlPlugin.COPY_INCLUDED_IDL_TASK_NAME)

    project.tasks.getByName('generateInterfaces').doFirst {
      if (useFinagle)
        args '--finagle'
      args (['-d', outputDirs.getSingleFile(), '-l', getLanguage()])
      args (['-i', project.fileTree((Object){project.dependencyIdlDir}).getDir()])
      args (['-i', includedFiles.dir])
      args inputFiles.files

      classpath project.configurations.getByName(SCROOGE_GEN_CONFIGURATION)
    }

    //Even if it's a scala project, it could still have mixed java and scala code, so make sure we generate
    //interfaces before we try to compile anything
    project.tasks.getByName('compileJava') {
      dependsOn 'generateInterfaces'
    }

    def mainSourceSet = getMainSourceSet(project)
    mainSourceSet.srcDir {project.thriftGenDir}

    project.tasks.getByName('jar').from {project.thriftSrcDir}
  }

  abstract protected getMainSourceSet(Project project);
  abstract protected String getLanguage();
}
