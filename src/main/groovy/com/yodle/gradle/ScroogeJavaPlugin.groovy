package com.yodle.gradle

import org.gradle.api.Project

class ScroogeJavaPlugin extends ScroogePlugin {
  @Override protected getMainSourceSet(Project project) {
    return project.sourceSets.main.java;
  }

  @Override protected String getLanguage() {
    return 'java';
  }
}
