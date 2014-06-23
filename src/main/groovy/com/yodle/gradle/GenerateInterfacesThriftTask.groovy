package com.yodle.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.TaskAction

class GenerateInterfacesThriftTask extends DefaultTask {
  @InputFiles FileCollection inputFiles
  @InputFiles FileCollection includedFiles
  @InputFiles FileCollection dependencyFiles
  @OutputDirectories FileCollection outputDirs
  String language
  String generator = 'thrift'
  private boolean onWindows = System.properties['os.name'].toLowerCase().contains('windows')

  @TaskAction
  public void exec() {
    def args =['-o', outputDirs.getSingleFile(), '--gen', language]
    args += ['-I', dependencyFiles.dir]
    args += ['-I', includedFiles.dir]

    inputFiles.files.each {
      generate(it, args)
    }

    dependencyFiles.files.each {
      generate(it, args)
    }
  }

  def generate(File thriftFile, ArrayList<Serializable> generatorArgs) {
    if (onWindows) {
      project.exec {
        commandLine 'cmd', '/c', generator
        args generatorArgs + thriftFile.path
      }
    } else {
      project.exec {
        executable generator
        args generatorArgs + thriftFile.path
      }
    }
  }
}
