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

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
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
