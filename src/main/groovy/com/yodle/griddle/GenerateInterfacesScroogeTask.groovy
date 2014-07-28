/*
 * Copyright 2014 Yodle, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
