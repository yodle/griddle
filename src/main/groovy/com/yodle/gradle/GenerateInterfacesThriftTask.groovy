package com.yodle.gradle

import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.OutputDirectories

class GenerateInterfacesThriftTask extends Exec {
  @InputFiles FileCollection inputFiles
  @InputFiles FileCollection includedFiles
  @InputFiles FileCollection dependencyFiles
  @OutputDirectories FileCollection outputDirs
  String language

  public configure() {
    doFirst {
      args (['-o', outputDirs.getSingleFile(), '--gen', language])
      args (['-I', dependencyFiles.dir])
      args (['-I', includedFiles.dir])
      args inputFiles.files
      args dependencyFiles.files

      executable 'thrift'
    }
  }
}
