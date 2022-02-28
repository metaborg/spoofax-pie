# Spoofax 3 language workbench distribution

The *language workbench distribution* part of Spoofax 3. It depends on the `spoofax.lwb.compiler.gradle` Gradle plugins
from `lwb`, and is therefore in a separate included build, as Gradle plugins are only available through included builds.

### Directory structure

```
.
├── lang                           # Target languages
│   ├── rv32im                     # RISC-32 intermediate language
├── spoofax.lwb.eclipse            # Eclipse language workbench plugin
├── spoofax.lwb.eclipse.feature    # Eclipse language workbench feature
├── spoofax.lwb.eclipse.repository # Eclipse language workbench repository
├── build.gradle.kts               # Gradle build configuration
├── settings.gradle.kts            # Gradle build settings (multi-project)
└── README.md
```
