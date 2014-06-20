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
      def idl = inputFiles.collect {
        project.zipTree(it.path).files
      }.flatten()
      idl.removeAll {
        !it.path.endsWith('.thrift')
      }

      project.copy {
        from idl
        into outputDirs.singleFile
      }
    }
  }

}
