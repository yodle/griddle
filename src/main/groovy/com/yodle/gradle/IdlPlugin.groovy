package com.yodle.gradle

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar

class IdlPlugin implements Plugin<Project> {
  public static final String IDL_CONFIGURATION = 'idl'
  public static final String COMPILED_IDL_CONFIGURATION = 'compiledIdl'
  public static final String COPY_DEPENDENCY_IDL_TASK_NAME = 'copyDependencyIdl'
  public static final String COPY_INCLUDED_IDL_TASK_NAME = 'copyIncludedIdl'

  @Override void apply(Project project) {
    project.configurations.create(IDL_CONFIGURATION)
    project.configurations.create(COMPILED_IDL_CONFIGURATION)

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
    project.tasks.getByName('assemble').dependsOn 'idlJar'
    project.tasks.getByName('idlJar').from {project.thriftSrcDir}

    project.artifacts.add(IDL_CONFIGURATION, project.tasks.getByName('idlJar'))
  }
}
