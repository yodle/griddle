package com.yodle.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.OutputDirectories

class GenerateInterfacesTask extends JavaExec
{
  @InputFiles FileCollection inputFiles
  @OutputDirectories FileCollection outputDirs
  boolean useFinagle = true
}
