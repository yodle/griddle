package com.yodle.gradle

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar

abstract class ScroogePlugin implements Plugin<Project> {

  private static final String SCROOGE_GEN_CONFIGURATION = 'scroogeGen'

  void apply(Project project) {
    project.plugins.apply(getLanguage())
    project.configurations.create(SCROOGE_GEN_CONFIGURATION)

    project.ext.set('thriftSrcDir',"${project.getProjectDir().getPath()}/src/main/thrift")
    project.ext.set('thriftGenDir', "${project.getProjectDir().getPath()}/build/gen-src")
    project.tasks.create('generateInterfaces', GenerateInterfacesTask.class, new Action<GenerateInterfacesTask>() {
      @Override void execute(GenerateInterfacesTask t)
      {
        t.inputFiles = project.fileTree((Object){project.thriftSrcDir})
        t.outputDirs = project.files((Object){project.thriftGenDir})
        t.setMain('com.twitter.scrooge.Main')
      }
    })

    project.tasks.getByName('generateInterfaces').doFirst {
      if (useFinagle)
        args '--finagle'
      args (['-d', outputDirs.getSingleFile(), '-l', getLanguage()])
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

    project.tasks.create('idlJar', Jar.class, new Action<Jar>(){
      @Override void execute(Jar t) {
        t.classifier = 'idl'
      }
    });
    project.tasks.getByName('assemble').dependsOn 'idlJar'

    project.tasks.getByName('jar').from {project.thriftSrcDir}
    project.tasks.getByName('idlJar').from {project.thriftSrcDir}
  }

  abstract protected getMainSourceSet(Project project);
  abstract protected String getLanguage();
}
