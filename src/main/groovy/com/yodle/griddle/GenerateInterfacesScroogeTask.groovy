package com.yodle.griddle

import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.OutputDirectories

class GenerateInterfacesScroogeTask extends JavaExec {
  @InputFiles FileCollection inputFiles
  @InputFiles FileCollection includedFiles
  @InputFiles FileCollection dependencyFiles
  @OutputDirectories FileCollection outputDirs
  String language
  boolean useFinagle = true;

  public configure() {
    doFirst {
      if (useFinagle)
        args '--finagle'
      args (['-d', outputDirs.getSingleFile(), '-l', language])
      args (['-i', dependencyFiles.dir])
      args (['-i', includedFiles.dir])
      args inputFiles.files
      args dependencyFiles.files

      classpath project.configurations.getByName(ScroogePlugin.SCROOGE_GEN_CONFIGURATION)
    }
  }
}
