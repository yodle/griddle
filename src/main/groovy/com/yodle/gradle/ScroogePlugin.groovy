package com.yodle.gradle

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import org.gradle.api.tasks.bundling.Jar

abstract class ScroogePlugin implements Plugin<Project> {

  private static final String SCROOGE_GEN_CONFIGURATION = 'scroogeGen'
  private static final String IDL_CONFIGURATION = 'idl'

  void apply(Project project) {
    project.plugins.apply(getLanguage())
    project.configurations.create(SCROOGE_GEN_CONFIGURATION)
    project.configurations.create(IDL_CONFIGURATION)

    project.ext.set('thriftSrcDir',"${project.getProjectDir().getPath()}/src/main/thrift")
    project.ext.set('thriftGenDir', "${project.getProjectDir().getPath()}/build/gen-src")
    project.ext.set('dependencyIdlDir', "${project.getProjectDir().getPath()}/build/idl/dependency")
    project.tasks.create('generateInterfaces', GenerateInterfacesTask.class, new Action<GenerateInterfacesTask>() {
      @Override void execute(GenerateInterfacesTask t)
      {
        t.inputFiles = project.files(
                project.fileTree((Object){project.thriftSrcDir}),
                project.fileTree((Object){project.dependencyIdlDir})
        );
        t.outputDirs = project.files((Object){project.thriftGenDir})
        t.setMain('com.twitter.scrooge.Main')
      }
    })

    project.tasks.getByName('generateInterfaces').dependsOn project.configurations.getByName(IDL_CONFIGURATION)

    project.tasks.getByName('generateInterfaces').doFirst {
      def dependencyIdl = project.configurations.getByName(IDL_CONFIGURATION).collect {
        project.zipTree(it.path).files
      }.flatten()
      dependencyIdl.removeAll {
        !it.path.endsWith('.thrift')
      }

      project.copy {
        from dependencyIdl
        into {project.dependencyIdlDir}
      }

      if (useFinagle)
        args '--finagle'
      args (['-d', outputDirs.getSingleFile(), '-l', getLanguage()])
      args (['-i', project.fileTree((Object){project.dependencyIdlDir}).getDir()])
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

    project.artifacts.add(IDL_CONFIGURATION, project.tasks.getByName('idlJar'))
  }

  abstract protected getMainSourceSet(Project project);
  abstract protected String getLanguage();
}
