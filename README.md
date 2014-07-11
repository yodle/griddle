# Griddle

The Griddle project is a set of plugins that empower Gradle to understand how to manage and generate thrift files.  These plugins provide features such as

* Exporting the idl of a project so that any consumer can generate that idl in the language of its choice with the generator of its choice
* Allowing idl files in one project to `include` idl files that live in another gradle subproject, in another gradle project, or in any arbitrary jar
* Easy, portable generation using Twitter's [Scrooge](https://github.com/twitter/scrooge) generator or the default [Apache Thrift](https://thrift.apache.org/) generator

# Plugins

## The Idl Plugin

The `idl` plugin enables Gradle to understand and manage idl based dependencies and provides basic support for projects that want to generate thrift files.  It provides 

* Two new Configurations
* Three new tasks: `idlJar`, `copyDependencyIdl`, and `copyIncludedIdl`
* Options to control the directories containing thrift files or used by the thrift generation process

### Configurations

#### idl

The `idl` configuration indicates that the dependency (and/or one or more of its transitive dependencies) has exported thrift files in its jars, and those thrift files should be generated by a consuming project.  All `idl` dependencies are automatically added to the `compile` configuration

#### compiledIdl

The `compiledIdl` configuration, much like the `idl` configuration, indicates that the dependency and/or one or more of its transitive dependencies) has exported thrift files in its jars.  However unlike the `idl` configuration, those thrift files have already been generated.  This is used to indicate dependencies whose exported thrift files may be `include`ed by the dependent project.  All `compiledIdl` depenencies are added to the `compile` configuration.

### Tasks

#### idlJar

By default, all source thrift files of projects using the `idl` plugin will be included in the default jar produced by the project.  In the event that you want a jar containing _only_ the thrift files of the project, you may use the `idlJar` task.  It will create a jar with the classifier `idl` containing only the source thrift files of the project.  The jar is added as an artifact of the project's `idl` configuration.  Thus other projects can specify that their dependency on this project use the `idl` configuration and get the idl jar, rather than the default jar.

#### copyDependencyIdl and copyIncludedIdl

These tasks are responsible for copying idl from `idl` and `compiledIdl` dependencies respectively into a defined location so that generators can generate or include them.  You should never need to manually configure these tasks.  Instead, utilize the configuration options below.

### Configuration Options

The `idl` plugin adds four configuration options directly to each gradle project using the `idl` plugin.

#### thriftSrcDir

This option indicates the directory containing the source thrift files for the project.  It defaults to "src/main/thrift".  Any directory structure under thriftSrcDir will be preserved when the thrift files are exported in the project's jar files.

#### thriftGenDir

This option indicates the directory that generators should generate real source files into.  It defaults to "build/gen-src".

#### dependencyIdlDir

This option indicates a staging directory for all thrift files provided by `idl` dependencies.  I.e. thrift files that must be generated.  Defaults to "build/idl/dependency"

#### includedIdlDir

This option indicates a staging directory for all thrift files provided by `compiledIdl` dependencies.  I.e. thrift files that may be included by other thrift files but should not themselves be genrated.  Defaults to "build/idl/included"

## The Thrift Plugin

In addition to applying the `idl` plugin, the `thrift` plugin adds the capability to generate thrift files using the standard Apache Thrift generator in any language supported by that generator.  It will also integrate the interface generation process into the Java build process.

### Tasks

#### generateInterfaces

The `generateInterfaces` task will automatically generate all thrift files provided by `idl` dependencies or under the `thriftSrcDir` specified for the project.  By default, it will generate java classes using the `java:hashcode` generator.  This can be overridden by setting the `language` property on the `generateInterfaces` task to any language supported by the thrift generator you are using.  The `generateInterfaces` task assumes that there will be a thrift generator binary named `thrift` on the `PATH`.  If you would rather explicitly specify the location of the thrift binary, you can override the `generator` property of the `generateInterfaces` task. 

## The Scrooge and Scrooge-Java plugins

Much like the `thrift` plugin, the `scrooge` and `scrooge-java` plugins integrate idl generation into the scala or java build processes respectively, using Twitter's Scrooge generator instead.

### Configurations

#### scroogeGen

The scrooge plugins adds a `scroogeGen` configuration.  This should be used to specify the dependency on the desired version of the scrooge generator jar.  This configuration is _not_ applied as a `compile` dependency, so you will need to additionally specify a `compile` dependency on the desired version of the scrooge runtime.

### Tasks

#### generateInterfaces

The `generateInterfaces` task will automatically generate all thrift files provided by `idl` dependencies or under the `thriftSrcDir` specified for the project.  The `scrooge` plugin will generate native scala interfaces while the `scrooge-java` plugin will generate java interfaces.  By default, they will both generate [finagled](https://github.com/twitter/finagle) interfaces.  This can be disabled by setting the `useFinagle` property on the `generateInterfaces` task to false.


# Interaction With Idea Plugin

If you are using the [IntelliJ Idea](http://www.gradle.org/docs/current/userguide/idea_plugin.html) plugin alongside the `thrift`, `scrooge`, or `scrooge-java` plugins, there are a couple extra steps you will need to take to ensure that your project imports successfully.  By default, the the generator projects generate into a folder under the build directory, however this directory is automatically marked as excluded by the idea plugin and will not be added as a project directory.  To get around this, you will either need to move the `thriftGenDir` outside the build directory or modify the idea plugin's exclude settings via:

    idea.module {
        excludeDirs -= file(buildDir)
        excludeDirs += file("${buildDir}/classes") //Not strictly necessary, but nice for cleanliness
        sourceDirs += file("${thriftGenDir}/gen-java") //For the thrift plugin only
    }

excluding any other subdirectories of the build dir that you would like.  Additionally, if you are using the `thrift` plugin, you will need to compensate for the fact that the thrift generator will always generate to a subdirectory of the directory you point it to.  For example, if you are generating java interfaces, you will need to add '${thriftGenDir}/gen-java' as a source directory.

Lastly, you need to make sure that you generate interfaces _before_ running `gradle idea`.  Otherwise the `thriftGenDir` directory will not yet exist and IDEA will not add it as a source directory.


# Sample Usages

## Single Project

The following is a sample single-project build.gradle file that is using the `scrooge` plugin to generate thrift files that are located in the non-standard directory 'resources/idl'.

`build.gradle`

    apply plugin: 'scrooge'

    repositories {
        mavenCentral()
    }

    dependencies {
        // Selects which generator to use for the idl
        scroogeGen "com.twitter:scrooge-generator_2.10:3.13.2"

	//Compile time dependencies for the generated interfaces
        compile "org.scala-lang:scala-library:2.10.3"
        compile "com.twitter:scrooge-runtime_2.10:3.13.2"
        compile "org.apache.thrift:libthrift:0.5.0"
    }

    //non-standard configuration
    thriftSrcDir = "${projectDir}/resources/idl"

## Heterogeneous Generators

This example shows how `idl` dependencies can be chained along and generated in a terminal 'consumer' project, each of which is responsible for generating the idl in the manner that it chooses.  This is useful when you have a few projects each of which is set up to use their own generators or if you do not want to share compiled code across repositories.  The projects in the example are as follows:

* 'idl-base' contains a base thrift file,
* 'idl-dependent' is an idl project whose files `include` the base thrift file in `idl-base`
* 'java-consumer' is a java project which has an `idl` dependency on the idl projects so that it can generate them locally using Thrift
* 'scala-consumer is a scala project which has an `idl` dependency on the idl projects so that it can generate them locally using Scrooge

Note that the below examples exclude the specification of repositories and additional compile-time dependencies for the sake of brevity

`idl-base/build.gradle`

    apply plugin: 'idl'

`idl-dependent/build.gradle`

    apply plugin: 'idl'

    dependencies {
        idl project(':idl-base')
    }  

`java-consumer/build.gradle`

    apply plugin: 'thrift'
    
    dependencies {
        idl project(':idl-dependent')
    }

`scala-consumer/build.gradle`

    apply plugin: 'scrooge'

    dependencies {
        idl project(':idl-dependent')
    }

## Incremental Generation

The above approach works well if you need the flexibility of each consumer generating interfaces for itself or if different consumers are under the control of different teams.  However this can lend itself to inefficiency as the same interfaces get generated and compiled multiple times.  This example shows how to perform incremental generation of the interfaces so that they are only generated and compiled once, albeit at the cost of only allowing for one generator type.

* 'idl-base' contains a base thrift file,
* 'idl-dependent' is an idl project whose files `include` the base thrift file in `idl-base`
* 'consumer' is a java project which consumes the already generated interfaces of the idl projects

`idl-base/build.gradle`

    apply plugin: 'thrift'

`idl-dependent/build.gradle`

    apply plugin: 'thrift'

    dependencies {
        //we specify a compiledIdl dependency because we want to specify that the dependency contains thrift files, but we don't want to generate them as we would an idl dependency
        compiledIdl project(':idl-base')
    }

`consumer/build.gradle`

    apply plugin: 'java'

    dependencies {
        compile project(':idl-dependent')
    }


## Incremental Generation With Multiple Generators

This example demonstrates the best way we've currently found of enabling incremental generation with multiple different generators.  It does begin to increase the complexity of the project layout, especially in the presence of idl dependencies between projects, but saves a substantial amount of computation over having each consumer of the interfaces generate and compile the same interfaces over and over.  This example also demonstrates how incremental generation can co-exist with non-incremental generation.

* 'idl-base' contains a base thrift file
* 'idl-base/thrift' is responsible for generating the base thrift file using Thrift
* 'idl-base/scrooge' is responsible for generating the base thrift file using Scrooge
* 'idl-dependent' contains a thrift file which `include`s the file in `idl-base`
* 'idl-dependent/thrift' is responsible for generating `idl-dependent`'s idl using Thrift
* 'idl-dependent/scrooge' is responsible for generating `idl-dependent`'s idl using Scrooge
* 'thrift-consumer' consumes the thrift interfaces generated by `idl-dependent/thrift`
* 'scala-consumer' consumes the thrift interfaces generated by `idl-dependent/scrooge`
* 'non-incremental-consumer' is a consumer that ignores the incrementally generated interfaces and generates all of its dependencies itself

`idl-base/build.gradle`

    apply plugin: 'idl'

`idl-base/thrift/build.gradle`

    apply plugin: 'thrift'
    
    dependencies {
        idl project.getParent() 
    }

`idl-base/scrooge/build.gradle`

    apply plugin: 'scrooge'
    
    dependencies {
        idl project.getParent() 
    }

`idl-dependent/build.gradle`

    apply plugin: 'idl'

    dependencies {
        idl project(':idl-base')
    }

`idl-dependent/thrift/build.gradle`

    apply plugin: 'thrift'

    dependencies {
        compile project(':idl-base:thrift')

        //this example uses project.getParent() rather than explicit dependencies on the parent because this setup can be extracted into an included .gradle file so it can be shared by a number of projects
        idl (project.getParent()) { transitive=false } //We exclude transitive dependencies here because we only want to generate the idl that is native to idl-dependent
        compiledIdl (project.getParent()) //But we need a compiledIdl dependency on our parent's transitive deps because those dependencies are included by our parent's thrift files
    }

`idl-dependent/thrift/build.gradle`

    apply plugin: 'scrooge'

    dependencies {
        compile project(':idl-base:scrooge')

        idl (project.getParent()) { transitive=false }
        compiledIdl (project.getParent())
    }

`thrift-consumer/build.gradle`

    apply plugin: 'java'
    
    dependencies {
        compile project(':idl-dependent:thrift')
    }

`scrooge-consumer/build.gradle`

    apply plugin: 'scala'
    
    dependencies {
        compile project(':idl-dependent:scrooge')
    }

`non-incremental-consumer/build.gradle`

    apply plugin: 'thrift'

    dependencies {
      idl project(':idl-dependent')
    }


## Idl Dependencies Via Jar

All the above examples demonstrate idl dependencies being propogated via gradle project dependencies.  However the gradle project that hosts the thrift files may not live in the same overall build as the project that consumes them.  This example demonstrates one set of projects producing idl jars and a separate project depending on them through an intermediate maven repo (on the local filesystem) rather than via a direct gradle project dependcency.


* 'idl-base' contains a base thrift file
* 'idl-dependent' contains a thrift file which `include`s the file in `idl-base`
* 'consumer' generates the thrift files provided by the idl projects as a jar retrieved through maven

`idl-base/build.gradle`

    apply plugin: 'idl'
    apply plugin: 'maven'

    group = 'com.yodle.griddle.example'
    version = '0.1'
    jar.baseName = 'idl-base'
    
    uploadArchives {
        repositories {
            mavenDeployer {
	        repository(url: "file://localhost/${projectDir}/.repo")
            }
        }
    }

`idl-dependent/build.gradle`

    apply plugin: 'idl'
    apply plugin: 'maven'

    group = 'com.yodle.griddle.example'
    version = '0.1'
    jar.baseName = 'idl-dependent'

    dependencies {
        project(":idl-base")
    }
    
    uploadArchives {
        repositories {
            mavenDeployer {
	        repository(url: "file://${projectDir}/.repo")
            }
        }
    }

`consumer/build.gradle`

    apply plugin: 'thrift'

    repositories {
        url "${projectDir}/.repo"
    }

    dependencies {
        idl 'com.yodle.griddle.example:idl-dependent:0.1'
    }



    




