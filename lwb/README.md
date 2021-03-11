# Spoofax 3 language workbench

The *language workbench* part of Spoofax 3. It depends on the `spoofax.compiler.gradle` and
`spoofax.compiler.gradle.spoofax2` Gradle plugins from `core`, and is therefore in a separate
included build, as Gradle plugins are only available through included builds.

### Directory structure

```
.
├── metalang                     # Meta-languages
│   ├── cfg                        # Language/compiler configuration meta-DSL
│   ├── esv                        # Wrapper around Spoofax 2's ESV
│   ├── sdf3                       # Wrapper around Spoofax 2's SDF3
│   ├── statix                     # Wrapper around Spoofax 2's Statix
│   ├── stratego                   # Wrapper around Spoofax 2's Stratego and incremental compiler
├── metalib                      # Meta-libraries (mimicking source dependencies)
│   ├── libspoofax2                # Wrapper around Spoofax 2's meta.lib.spoofax
│   ├── libstatix                  # Wrapper around Spoofax 2's statix.runtime
├── spoofax.lwb.compiler         # Incremental compilation from language specification to Java library
├── spoofax.lwb.compiler.cfg     # Compiler configuration. Separate project so that the cfg meta-DSL
│                                  can provide this configuration, and the compiler can consume it
├── spoofax.lwb.compiler.dagger  # Compiler Dagger integration. Separate project to resolve ordering
│                                  problems between annotation processors.
├── spoofax.lwb.compiler.gradle  # Compiler Gradle plugin
├── spoofax.lwb.dynamicloading   # Dynamic loading for interactive language development
├── spoofax.lwb.eclipse          # Eclipse language workbench plugin
├── build.gradle.kts             # Gradle build configuration
├── settings.gradle.kts          # Gradle build settings (multi-project)
└── README.md
```
