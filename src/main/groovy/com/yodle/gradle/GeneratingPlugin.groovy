package com.yodle.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

abstract class GeneratingPlugin implements Plugin<Project> {
  public static final GENERATE_INTERFACES_TASK_NAME = 'generateInterfaces'
  @Override
  void apply(Project project) {
    project.plugins.apply('idl')
    project.configurations.getByName('compile').extendsFrom project.configurations.getByName(IdlPlugin.COMPILED_IDL_CONFIGURATION)

    def generateInterfacesTask = createGenerateInterfacesTask(project)
    generateInterfacesTask.dependsOn project.tasks.getByName(IdlPlugin.COPY_DEPENDENCY_IDL_TASK_NAME)
    generateInterfacesTask.dependsOn project.tasks.getByName(IdlPlugin.COPY_INCLUDED_IDL_TASK_NAME)


    //Even if it's a scala project, it could still have mixed java and scala code, so make sure we generate
    //interfaces before we try to compile anything
    project.tasks.getByName('compileJava') {
      dependsOn GENERATE_INTERFACES_TASK_NAME
    }

    def mainSourceSet = getMainSourceSet(project)
    mainSourceSet.srcDir {project.thriftGenDir}

    project.tasks.getByName('jar').from {project.thriftSrcDir}
  }

  abstract protected getMainSourceSet(Project project);
  abstract protected Task createGenerateInterfacesTask(Project project)
}
