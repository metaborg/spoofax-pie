---
title: "Eclipse Project Files"
---
# Eclipse Project Files
To be able to import a Spoofax 3 project into Eclipse, it should have at least the `.project` and
`.classpath` project files. Those files live in the root of the project (i.e., where the
`spoofaxc.cfg` file lives), with the following minimum content:

!!! warning ""
    Eclipse will adjust these files as needed.

!!! tip "Ensure these files are not in `.gitignore`"
    Often these Eclipse files are ignored for version control by specifying them
    in `.gitignore`. To allow these projects to be imported in the future, do not ignore them when
    committing the files.

!!! info ""
    Adjust the `<name>myproject</name>` to be the name of the project.

```xml title=".project"
<?xml version="1.0" encoding="UTF-8"?>
<projectDescription>
    <name>myproject</name>
    <comment></comment>
    <projects>
    </projects>
    <buildSpec>
        <buildCommand>
            <name>spoofax.lwb.eclipse.builder.project.references</name>
            <arguments>
            </arguments>
        </buildCommand>
        <buildCommand>
            <name>spoofax.lwb.eclipse.builder</name>
            <triggers>clean,full,incremental,</triggers>
            <arguments>
            </arguments>
        </buildCommand>
        <buildCommand>
            <name>org.eclipse.jdt.core.javabuilder</name>
            <arguments>
            </arguments>
        </buildCommand>
    </buildSpec>
    <natures>
        <nature>org.eclipse.jdt.core.javanature</nature>
        <nature>spoofax.lwb.eclipse.nature</nature>
    </natures>
</projectDescription>
```

```xml title=".classpath"
<?xml version="1.0" encoding="UTF-8"?>
<classpath>
    <classpathentry kind="src" path="src/main/java">
        <attributes>
            <attribute name="optional" value="true"/>
        </attributes>
    </classpathentry>
    <classpathentry kind="src" path="build/generated/sources/language">
        <attributes>
            <attribute name="optional" value="true"/>
            <attribute name="ignore_optional_problems" value="true"/>
        </attributes>
    </classpathentry>
    <classpathentry kind="src" path="build/generated/sources/adapter">
        <attributes>
            <attribute name="optional" value="true"/>
            <attribute name="ignore_optional_problems" value="true"/>
        </attributes>
    </classpathentry>
    <classpathentry kind="src" path="build/generated/sources/eclipse">
        <attributes>
            <attribute name="optional" value="true"/>
            <attribute name="ignore_optional_problems" value="true"/>
        </attributes>
    </classpathentry>
    <classpathentry kind="src" path="build/generated/sources/metalang/java">
        <attributes>
            <attribute name="optional" value="true"/>
            <attribute name="ignore_optional_problems" value="true"/>
        </attributes>
    </classpathentry>
    <classpathentry kind="src" path="build/generated/sources/annotationProcessor/java/main">
        <attributes>
            <attribute name="optional" value="true"/>
            <attribute name="ignore_optional_problems" value="true"/>
        </attributes>
    </classpathentry>
    <classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>
    <classpathentry kind="output" path="build/eclipseclasses"/>
</classpath>
```
