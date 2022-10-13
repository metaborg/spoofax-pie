# Spoofax 3 language workbench

The *language workbench* part of Spoofax 3. It depends on the `spoofax.compiler.gradle` and
`spoofax.compiler.gradle.spoofax2` Gradle plugins from `core`, and is therefore in a separate
included build, as Gradle plugins are only available through included builds.

### Directory structure

```
.
├── metalang                      # Meta-languages
│   ├── cfg                          # Compiler configuration meta-DSL
|   ├── dynamix                      # Dynamix DSL
|   ├── dynamix_runtime              # Runtime strategies for Dynamix
│   ├── esv                          # Wrapper around Spoofax 2's ESV
|   ├── llvm                         # Spoofax implementation of LLVM IR (target language of dynamix)
│   ├── sdf3                         # Wrapper around Spoofax 2's SDF3
|   ├── sdf3_ext_dynamix             # Signature generation for dynamix
│   ├── sdf3_ext_statix              # Wrapper around Spoofax 2's sdf3.ext.statix, implementing Statix
│                                      signature and injection implication/explication generation
│   ├── spt                          # Wrapper around Spoofax 2's SPT
│   ├── statix                       # Wrapper around Spoofax 2's Statix
│   ├── stratego                     # Wrapper around Spoofax 2's Stratego 2 and incremental compiler
|   ├── tim                          # Target intermediate language for Dynamix
|   ├── tim_runtime                  # Runtime strategies for Tim
├── metalib                        # Meta-libraries (mimicking source dependencies)
│   ├── libspoofax2                  # Wrapper around Spoofax 2's meta.lib.spoofax
│   ├── libstatix                    # Wrapper around Spoofax 2's statix.runtime
│   ├── strategolib                  # Wrapper around Spoofax 2's strategolib (Stratego 2 standard library)
│   ├── strategolib                  # Wrapper around Spoofax 2's gpp (Stratego 2 pretty-printing library)
├── spoofax.lwb.compiler           # Incremental compilation from language definition to Java library
├── spoofax.lwb.compiler.gradle    # Language definition compiler Gradle plugin
├── spoofax.lwb.dynamicloading     # Dynamic loading for interactive language development
├── build.gradle.kts               # Gradle build configuration
├── settings.gradle.kts            # Gradle build settings (multi-project)
└── README.md
```
