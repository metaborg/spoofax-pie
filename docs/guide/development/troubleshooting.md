# Troubleshooting

This guide explains how to troubleshoot issues when developing Spoofax itself.

In general, ensure you're calling `./repo` and `./gradlew` on Linux and MacOS (or `repo.bat` and `gradlew.bat` on Windows) instead of your local Gradle installation. The local one may be too old or too new.

## Known Problems
The following are known problems that can occur, and their solutions or workarounds.

### Many errors about unresolved classes in IntelliJ
If after importing there are many errors in files about classes not existing, re-import all the projects by pressing the `Reload All Gradle Projects` button in the Gradle tool window.

### Cannot debug in IntelliJ
See [how to debug in IntelliJ](debugging-in-intellij.md) for more information and tips.

### Profiling in IntelliJ
Profiling in IntelliJ can be done similarly to debugging. For example, to profile with YourKit, add the following environment variable to your run configuration:

```
JAVA_TOOL_OPTIONS=-agentpath:/Applications/YourKit-Java-Profiler-2020.9.app/Contents/Resources/bin/mac/libyjpagent.dylib=listen=all,sampling,onexit=snapshot
```

If you are using a different profiler, the `agentpath` needs to point to the corresponding agent of your profiler, and the settings after the agent will need to be tailored towards your profiler.
In the example above, the YourKit profiler will attach to the program, enable CPU sampling, and create a snapshot when the program ends.
The snapshot can then be opened and inspected in YourKit.

Similar to debugging, this enables profiling for any Gradle task that executes Java in an isolated way, and tests must be cleaned before profiling to force tests to be executed.


### Spoofax 2 language fails to build with "Previous build failed and no change in the build input has been observed"
If building a Spoofax 2 language fails due to some ephemeral issue, or if building is cancelled (because you cancelled the Gradle build), the following exception may be thrown during the build:

```
org.metaborg.core.MetaborgException: Previous build failed and no change in the build input has been observed, not rebuilding. Fix the problem, or clean and rebuild the project to force a rebuild
```

This is an artefact of the Pluto build system refusing to rebuild if it failed but no changes to the input were detected.
To force Pluto to rebuild, delete the `target/pluto` directory of the language.

### Task 'buildAll' not found in root project 'devenv'
You have 'configure on demand' enabled, such as `org.gradle.configureondemand=true` in your `~/.gradle/gradle.properties` file. Disable this.

### Expiring Daemon because JVM heap space is exhausted
The memory limits in `gradle.properties` may be too low, and may need to be increased.
Running the build without `--parallel` may decrease memory pressure, as less tasks are executed concurrently.
Or, there is a memory leak in the build: please make a heap dump and send this to the developers so it can be addressed.

### Could not create service of type FileAccessTimeJournal using GradleUserHomeScopeServices.createFileAccessTimeJournal()
The permissions in your `~/.gradle/` directory are too restrictive. For example, if you're using WSL, ensure the directory is not a symlink to the Windows' `.gradle/` directory.

### Error resolving plugin: Plugin request for plugin already on the classpath must not include a version

> Error resolving plugin [id: 'org.metaborg.gradle.config.devenv', version: '?']
>
> Plugin request for plugin already on the classpath must not include a version

You are not running with the recommended version of Gradle.

### Unknown command-line option '--args'
Command-line arguments such as `--args` are not supported for tasks in the root project, such as the `runSdf3Cli` task. Instead, go to the relevant included build and call the task directly.

```
cd spoofax.pie/example
./gradlew :sdf3.cli:run --args="-V"
```

The working directory is the directory with the `gradle.build.kts` file of the CLI project. This cannot be changed. For example, `spoofax.pie/example/sdf3/sdf3.cli/` for the `:sdf3.cli` project.


### Failed to create Jar file
Due to a bug in the Java 8 and 9 compiler, it sometimes generates the [wrong byte code for annotations](https://bugs.openjdk.java.net/browse/JDK-8144185). This, in turn, causes Gradle to fail reading the JAR file with the following error:

```
A problem occurred configuring project ':spoofax3.example.root'.
> Failed to create Jar file /Users/username/.gradle/caches/jars-8/defabc/jsglr.common-develop-SNAPSHOT.jar.
```

The stack trace will look something like this:

```
org.gradle.api.ProjectConfigurationException: A problem occurred configuring project ':spoofax3.example.root'.
        at org.gradle.configuration.project.LifecycleProjectEvaluator.wrapException(LifecycleProjectEvaluator.java:75)
        at org.gradle.configuration.project.LifecycleProjectEvaluator.addConfigurationFailure(LifecycleProjectEvaluator.java:68)
        at org.gradle.configuration.project.LifecycleProjectEvaluator.access$400(LifecycleProjectEvaluator.java:51)
        ... 142 more
Caused by: org.gradle.api.GradleException: Failed to create Jar file /Users/username/.gradle/caches/jars-8/defabc/jsglr.common-develop-SNAPSHOT.jar.
        at org.gradle.internal.classpath.ClasspathBuilder.jar(ClasspathBuilder.java:47)
        at org.gradle.internal.classpath.InstrumentingClasspathFileTransformer.instrument(InstrumentingClasspathFileTransformer.java:83)
        ... 6 more
Caused by: java.lang.ArrayIndexOutOfBoundsException: 195
        at org.objectweb.asm.ClassReader.readLabel(ClassReader.java:2654)
        at org.objectweb.asm.ClassReader.createLabel(ClassReader.java:2670)
        at org.objectweb.asm.ClassReader.readTypeAnnotations(ClassReader.java:2736)
        at org.objectweb.asm.ClassReader.readCode(ClassReader.java:1912)
        at org.objectweb.asm.ClassReader.readMethod(ClassReader.java:1492)
        at org.objectweb.asm.ClassReader.accept(ClassReader.java:717)
        ... 15 more
```

Determine the version of Java using `java -version`. Java 8 and 9 can exhibit this problem. The solution is to update your Java to version 11 or later. You can use a tool such as [SDKMAN!](https://sdkman.io/) to easily manage the (default) versions of Java on your system.
