package com.yodle.griddle

import org.gradle.api.Project

class ScroogeScalaPlugin extends ScroogePlugin {
  @Override protected getMainSourceSet(Project project) {
    return project.sourceSets.main.scala
  }

  @Override protected String getLanguage() {
    return 'scala';
  }
}
