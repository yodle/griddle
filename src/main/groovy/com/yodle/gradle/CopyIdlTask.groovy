package com.yodle.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectories

class CopyIdlTask extends DefaultTask
{
  @InputFiles FileCollection inputFiles
  @OutputDirectories FileCollection outputDirs

  CopyIdlTask() {

    doFirst {
      project.copy {
        inputFiles.each {
          from project.zipTree(it.path)
          include "**/*.thrift"
        }
        into outputDirs.singleFile
        includeEmptyDirs false
      }
    }
  }

}
