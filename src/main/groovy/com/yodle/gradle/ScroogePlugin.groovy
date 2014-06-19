package com.yodle.gradle

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class ScroogePlugin implements Plugin<Project> {

  private static final String SCROOGE_GEN_CONFIGURATION = 'scroogeGen'

  void apply(Project project) {
    project.plugins.apply(getLanguage())

    project.configurations.create(SCROOGE_GEN_CONFIGURATION)
    def thriftGenDir = "${project.getProjectDir().getPath()}/build/gen-src"
    project.tasks.create('generateInterfaces', GenerateInterfacesTask.class, new Action<GenerateInterfacesTask>() {
      @Override void execute(GenerateInterfacesTask t)
      {
        t.inputFiles = project.fileTree("${project.getProjectDir().getPath()}/src/main/thrift")
        t.outputDirs = project.files(thriftGenDir)
        t.setMain('com.twitter.scrooge.Main')
      }
    })

    project.tasks.getByName('generateInterfaces').doFirst {
      if (useFinagle)
        args '--finagle'
      args (['-d', outputDirs.getSingleFile(), '-l', getLanguage()])
      args inputFiles.files
      classpath project.configurations.getByName(SCROOGE_GEN_CONFIGURATION)


      project.tasks.getByName('jar').from inputFiles
    }

    //Even if it's a scala project, it could still have mixed java and scala code, so make sure we generate
    //interfaces before we try to compile anything
    project.tasks.getByName('compileJava') {
      dependsOn 'generateInterfaces'
    }

    def mainSourceSet = getMainSourceSet(project)
    mainSourceSet.srcDir thriftGenDir
  }

  abstract protected getMainSourceSet(Project project);
  abstract protected String getLanguage();
}
