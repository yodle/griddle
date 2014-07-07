#!/bin/sh

./gradlew build
./gradlew -p test-projects clean -PincludeTestProjects=true 
./gradlew -p test-projects test -PincludeTestProjects=true

#there are certain tests that require running test twice as they test how griddle acts on projects that have already been generated
./gradlew -p test-projects test -PincludeTestProjects=true