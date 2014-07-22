package com.yodle.griddle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.plugins.ide.eclipse.EclipsePlugin

abstract class GeneratingPlugin implements Plugin<Project> {
  public static final GENERATE_INTERFACES_TASK_NAME = 'generateInterfaces'
  @Override
  void apply(Project project) {
    project.plugins.apply('idl')

    def generateInterfacesTask = createGenerateInterfacesTask(project)
    generateInterfacesTask.dependsOn project.tasks.getByName(IdlPlugin.COPY_DEPENDENCY_IDL_TASK_NAME)
    generateInterfacesTask.dependsOn project.tasks.getByName(IdlPlugin.COPY_INCLUDED_IDL_TASK_NAME)

    //Even if it's a scala project, it could still have mixed java and scala code, so make sure we generate
    //interfaces before we try to compile anything
    project.tasks.getByName('compileJava') {
      dependsOn GENERATE_INTERFACES_TASK_NAME
    }

    def mainSourceSet = getMainSourceSet(project)
    mainSourceSet.srcDir {getAdjustedThriftGenDir(project)}

    project.plugins.withType(IdeaPlugin) {
      project.ideaModule.doFirst {
        project.idea.module {
          def buildDir = project.buildDir
          //Idea will exclude the build dir,even if it's told to include a subdirectory of the build dir.  So override
          //the excludes so that the build dir isn't excluded but the other subdirectories we don't care about are.
          excludeDirs -= project.file("${buildDir}")
          excludeDirs += project.file("${buildDir}/classes")
          excludeDirs += project.file("${buildDir}/tmp")
          excludeDirs += project.file("${buildDir}/test-results")
          excludeDirs += project.file("${buildDir}/resources")
        }

        //If ideaModule runs before the folders created by generateInterfaces exist, it will not add them as a source dir
        //so make them now
        project.file("${getAdjustedThriftGenDir(project)}").mkdirs()
      }
    }

    project.plugins.withType(EclipsePlugin) {
      project.eclipseClasspath.doFirst {
        //If the eclipse tasks runs before the folders created by generateInterfaces exist, it will not add them as a source dir
        //so make them now
        project.file("${getAdjustedThriftGenDir(project)}").mkdirs()
      }
    }
  }

  abstract protected getMainSourceSet(Project project);
  abstract protected Task createGenerateInterfacesTask(Project project);
  protected String getAdjustedThriftGenDir(Project project) {
    return project.thriftGenDir;
  }
}
