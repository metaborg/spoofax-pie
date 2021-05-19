# Creating a language project

--8<-- "docs/_include/_all.md"

This tutorial gets you started with language development by creating a language project and changing various aspects of the language. First follow the [installation tutorial](install.md) if you haven't done so yet.

## Creating a new project

In Eclipse, open the new project dialog by choosing <span class="guilabel">File ‣ New ‣ Project</span> from the main menu.
In the new project dialog, select <span class="guilabel">Spoofax LWB ‣ Spoofax language project</span> and press <span class="guilabel">Next</span>.
In this wizard, you can customize the various names your language will use.
However, for the purpose of this tutorial, fill in `HelloWorld` as the name of the project, which will automatically fill in the other elements with defaults.
Then press <span class="guilabel">Finish</span> to create the project.
There should now be a project named `helloworld` in the <span class="guilabel">Package Explorer</span.

## Adding syntax

First we will add some syntax to the language.
Open the main SDF3 file `helloworld/src/start.sdf3` file by expanding the folders and double-clicking the file.
SDF3 is a meta-language (i.e., a language to describe languages) for describing the syntax of a language, from which Spoofax will derive the parser of your language.
Under the `#!sdf3 context-free syntax` section, replace the `#!sdf3 Start.Empty = <>` line with `#!sdf3 Start.Program = <<{Part " "}*>>`, indicating that the language accepts programs which consists of one or more parts.
Then add `#!sdf3 Part.Hello = <hello>` on a new line, indicating that one sort of part is the word `hello.`
Finally, add `#!sdf3 Part.World = <world>` on a new line, indicating that one sort of part is the word `world`.
The context-free syntax section should now look as follows:

```sdf3
context-free syntax

  Start.Program = <<{Part " "}*>>
  Part.Hello = <hello>
  Part.World = <world>
```

To test our change, build the project by clicking on the project in the <span class="guilabel">Package Explorer</span> and choosing <span class="guilabel">Project ‣ Build Project</span> from the main menu, or by pressing ++cmd+b++ on macOS or ++ctrl+b++ on others.
To see when the build is done, open the progress window by choosing <span class="guilabel">Window ‣ Show View ‣ Progress</span>.
If the progress view is empty, the build is done.
The initial build can be a bit slow because there is a lot of code to compile in the background.
Subsequent builds will be faster due to incrementalization.

Create a test file for your language by right-clicking the project and choosing <span class="guilabel">New ‣ File</span>, filling in `test.hel` as file name, and pressing <span class="guilabel">Finish</span>.
Type a valid sentence such as `hello world hello hello world` in this file, and it will highlight purple indicating that `hello` and `world` are keywords.
There will also be an error marker on the file because we have not yet updated the static semantics (i.e., type checker) of the language to handle the language constructs we just added.
That is ok for now, we will fix this later.

## Changing syntax highlighting

Now we will change the syntax highlighter of the language.
Open the main ESV file `helloworld/src/main.esv`.
ESV is a meta-language for describing the syntax highlighter.
Change the `#!esv keyword : 127 0 85 bold` line to `#!esv keyword: 0 0 150 bold` and build the project.
Then check your `test.hel` example file, it should now be highlighted blue.

To make iteration easier, you can drag the `test.hel` tab to the side of the screen to open the language definition and example file side-by-side.
You can play around with the coloring a bit and choose a style to your liking.
Remember to rebuild the project after making a change to the language definition.

## Adding a debugging command

It can be quite handy to look at the AST of a program that the parser of the language produces as a debugging tool.
To do that, we will write a *task definition* that produces the AST of a program by parsing it, we will add a *command* for that task, and finally we will bind the command to a *menu item* so that we can execute the command.

### Creating the task definition

A task definition is a piece of code that take some input, may read from or write to files, and produce some output.
A task definition is written as a class in Java, and needs to adhere to a certain interface.
For brevity, we usually just refer to a "task definition" by "task".

Let's start by creating the class for this task.
First, right-click the `helloworld/src/main/java` directory and choose <span class="guilabel">New ‣ Package</span>, replace the name with `mb.helloworld.task`, and press <span class="guilabel">Finish</span>.
Then, right-click the `mb.helloworld.task` package we just created and choose <span class="guilabel">New ‣ Class</span> and fill in `HelloWorldShowParsedAst` as name, then press <span class="guilabel">Finish</span>.
Replace the entire Java file with the following code:

```java
package mb.helloworld.task;

import java.io.Serializable;
import java.util.Objects;

import javax.inject.Inject;

import org.checkerframework.checker.nullness.qual.Nullable;

import mb.helloworld.HelloWorldClassLoaderResources;
import mb.helloworld.HelloWorldScope;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.stratego.common.StrategoUtil;

@HelloWorldScope
public class HelloWorldShowParsedAst implements TaskDef<HelloWorldShowParsedAst.Args, CommandFeedback> {
  public static class Args implements Serializable {
    private static final long serialVersionUID = 1L;

    public final ResourceKey file;

    public Args(ResourceKey file) {
      this.file = file;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if(this == o) return true;
      if(o == null || getClass() != o.getClass()) return false;
      final Args args = (Args)o;
      return file.equals(args.file);
    }

    @Override
    public int hashCode() {
      return Objects.hash(file);
    }

    @Override
    public String toString() {
      return "Args{" + "file=" + file + '}';
    }
  }


  private final HelloWorldClassLoaderResources classloaderResources;
  private final HelloWorldParse parse;

  @Inject
  public HelloWorldShowParsedAst(HelloWorldClassLoaderResources classloaderResources, HelloWorldParse parse) {
    this.classloaderResources = classloaderResources;
    this.parse = parse;
  }


  @Override
  public CommandFeedback exec(ExecContext context, Args args) throws Exception {
    context.require(classloaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
    final ResourceKey file = args.file;
    return context.require(parse.inputBuilder().withFile(file).buildAstSupplier()).mapOrElse(
      ast -> CommandFeedback.of(ShowFeedback.showText(StrategoUtil.toString(ast), "Parsed AST for '" + file + "'")),
      e -> CommandFeedback.ofTryExtractMessagesFrom(e, file)
    );
  }

  @Override
  public String getId() {
    return getClass().getName();
  }
}
```

!!! todo
    Explain this class.

### Registering the task definition

We must register this task in order for Spoofax to know about it.
Open the CFG `helloworld/spoofax.cfg` file.
The CFG meta-language is a configuration language where we configure and glue together the various aspects of your language.
Add the following configuration to the end of the file:

```cfg
let showParsedAst = task-def mb.helloworld.task.HelloWorldShowParsedAst
```

This registers the task definition class that we just created, and makes it available under the `showParsedAst` name in the configuration.

### Creating the command

To create the command, add the following configuration to the end of the `spoofax.cfg` file:

```cfg
let showParsedAstCommand = command-def {
  type = java mb.helloworld.command.HelloWorldShowParsedAstCommand
  task-def = showParsedAst
  args-type = java mb.helloworld.task.HelloWorldShowParsedAst.Args
  display-name = "Show parsed AST"
  description = "Shows the parsed AST"
  supported-execution-types = [Once, Continuous]
  parameters = [
    file = parameter {
      type = java mb.resource.ResourceKey
      required = true
      argument-providers = [Context(File)]
    }
  ]
}
```

!!! todo
    Explain this configuration.

Some properties set above are set to their conventional (default) value, or are optional, so we can leave them out. Replace the command definition with the following code:

```cfg
let showParsedAstCommand = command-def {
  task-def = showParsedAst
  display-name = "Show parsed AST"
  parameters = [
    file = parameter {
      type = java mb.resource.ResourceKey
      argument-providers = [Context(File)]
    }
  ]
}
```

!!! todo
    Explain conventions and optionals.

### Adding the menu item

To add the menu item, add the following configuration to the end of the `spoofax.cfg` file:

```cfg
editor-context-menu [
  menu "Debug" [
    command-action {
      command-def = showParsedAstCommand
      execution-type = Once
    }
    command-action {
      command-def = showParsedAstCommand
      execution-type = Continuous
    }
  ]
]
resource-context-menu [
  menu "Debug" [
    command-action {
      command-def = showParsedAstCommand
      execution-type = Once
      required-resource-types = [File]
    }
  ]
]
```

!!! todo
    Explain this configuration.

Build the project so that we can test our changes.

!!! bug
    Building the project at this point will result in an error. This can be worked around by first cleaning the project by choosing <span class="guilabel">Project ‣ Clean</span> from the main menu. This bug will be fixed in a future version.

Let's test the command.
Open the `test.hel` file and right-click inside the editor area to open the context menu.
From the editor context menu, select <span class="guilabel">HelloWorld ‣ Debug ‣ Show parsed AST</span>.
A new editor will pop up with the result of the command, showing the AST of your example file.
Close the result and now run <span class="guilabel">HelloWorld ‣ Debug ‣ Show parsed AST (continuous)</span>.
Drag the resulting editor to the side and edit the example project, the resulting editor will update whenever the example file changes.

We can also run the command by activating the `test.hel` editor by choosing <span class="guilabel">Spoofax ‣ Debug ‣ Show parsed AST</span> from the main menu.
Finally, we can run the command by right-clicking the `test.hel` file in the <span class="guilabel">Package Explorer</span> by choosing <span class="guilabel">Spoofax ‣ Debug ‣ Show parsed AST</span> from the context menu.

## Changing the static semantics

Now we will fix the static semantics of the language.
Open the main Statix file `helloworld/src/main.stx`
Statix is a meta-language for defining the static semantics of your language, which includes type checking.

First we will update the Statix specification to handle the new language constructs.
Replace the `#!statix signature` section with the following:

```statix
signature

  sorts Start constructors
    Program : list(Part) -> Start
  sorts Part constructors
    Hello : Part
    World : Part
```

This defines the AST signature of the language.
In a future version of Spoofax 3, this will be automatically derived from the SDF3 specification.

Now, replace the `#!statix programOk(Empty()).` line with `#!statix programOk(Program(parts)).`, meaning that we accept all programs consisting of parts, which is always true due to the syntax of the language.
The file should now look like this:

```statix
module main

signature

  sorts Start constructors
    Program : list(Part) -> Start
  sorts Part constructors
    Hello : Part
    World : Part

rules

  programOk : Start
  programOk(Program(parts)).
```

Build the project, and the error marker should disappear from your example program.

As a silly rule, we will add a warning to all instances of `world` in the program.
Add the following code to the end of the Statix definition:

```statix
  partOk : Part
  partOk(Hello()).
  partOk(World()) :- try { false } | warning $[World!].
  partsOk maps partOk(list(*))
```

This adds a `#!statix partOk` rule that lets all `#!statix Hello()` parts pass, but will add a warning to all `#!statix World()` parts.
`#!statix partsOk` goes over a list of parts and applies `#!statix partOk`.
Replace the `#!statix programOk(Program(parts)).` line with `#!statix programOk(Program(parts)) :- partsOk(parts).` to apply the `#!statix partsOk` rule.
Build the project, and a warning marker should appear under all instances of `world` in the program.

## Adding a transformation

Finally, we will define a transformation for our language and add a task, command-tasks, command, and menu item for it.
Open the main Stratego file `helloworld/src/main.str`.
Stratego is a meta-language for defining term (AST) transformations through rewrite rules.
We will add a silly transformation that replaces all instances of `#!stratego World()` with `#!stratego Hello()`.

Add the following code to the end of the Stratego file:

```stratego
rules

  replace-world: Hello() -> Hello()
  replace-world: World() -> Hello()
  replace-worlds = topdown(try(replace-world))
```

The `#!stratego replace-world` rule passes `#!stratego Hello()` terms but rewrites `#!stratego World()` terms to `#!stratego Hello()`.
The `#!stratego replace-worlds` strategy tries to apply `#!stratego replace-world` in a top-down manner over the entire AST.

Now we add a task and command-task for this transformation.
We define two separate tasks to keep separate

1. the act of transforming the program, and
2. feeding back the result of that transformation to the user that executes a command.

This practice later allows us to reuse the first task in a different task if we need to.
Right-click the `mb.helloworld.task` package and create the `HelloWorldReplaceWorlds` class and replace the entire Java file with:

```java
package mb.helloworld.task;

import mb.helloworld.HelloWorldClassLoaderResources;
import mb.helloworld.HelloWorldScope;
import mb.pie.api.ExecContext;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.stratego.pie.AstStrategoTransformTaskDef;

import javax.inject.Inject;
import java.io.IOException;

@HelloWorldScope
public class HelloWorldReplaceWorlds extends AstStrategoTransformTaskDef {
  private final HelloWorldClassLoaderResources classloaderResources;

  @Inject
  public HelloWorldReplaceWorlds(
    HelloWorldClassLoaderResources classloaderResources,
    HelloWorldGetStrategoRuntimeProvider getStrategoRuntimeProvider
  ) {
    super(getStrategoRuntimeProvider, "replace-worlds");
    this.classloaderResources = classloaderResources;
  }

  @Override public String getId() {
    return getClass().getName();
  }

  @Override protected void createDependencies(ExecContext context) throws IOException {
    context.require(classloaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
  }
}

```

!!! todo
    Explain this class.

Then create the `HelloWorldShowReplaceWorlds` class and replace the entire Java file with:

```java
package mb.helloworld.task;

import java.io.Serializable;
import java.util.Objects;

import javax.inject.Inject;

import org.checkerframework.checker.nullness.qual.Nullable;

import mb.helloworld.HelloWorldClassLoaderResources;
import mb.helloworld.HelloWorldScope;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.stratego.common.StrategoUtil;

@HelloWorldScope
public class HelloWorldShowReplaceWorlds implements TaskDef<HelloWorldShowReplaceWorlds.Args, CommandFeedback> {
    public static class Args implements Serializable {
        private static final long serialVersionUID = 1L;

        public final ResourceKey file;

        public Args(ResourceKey file) {
            this.file = file;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Args args = (Args) o;
            return file.equals(args.file);
        }

        @Override
        public int hashCode() {
            return Objects.hash(file);
        }

        @Override
        public String toString() {
            return "Args{" + "file=" + file + '}';
        }
    }


    private final HelloWorldClassLoaderResources classloaderResources;
    private final HelloWorldParse parse;
    private final HelloWorldReplaceWorlds replaceWorlds;

    @Inject
    public HelloWorldShowReplaceWorlds(HelloWorldClassLoaderResources classloaderResources, HelloWorldParse parse, HelloWorldReplaceWorlds replaceWorlds) {
        this.classloaderResources = classloaderResources;
        this.parse = parse;
        this.replaceWorlds = replaceWorlds;
    }


    @Override
    public CommandFeedback exec(ExecContext context, Args args) throws Exception {
        context.require(classloaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        final ResourceKey file = args.file;
        return context.require(replaceWorlds, parse.inputBuilder().withFile(file).buildAstSupplier()).mapOrElse(
            ast -> CommandFeedback.of(ShowFeedback.showText(StrategoUtil.toString(ast), "Replaced World()s with Hello()s for '" + file + "'")),
            e -> CommandFeedback.ofTryExtractMessagesFrom(e, file)
        );
    }

    @Override
    public String getId() {
        return getClass().getName();
    }
}
```

This class very similar to `HelloWorldShowParsedAst`, but runs the `HelloWorldReplaceWorlds` task on the parsed AST, transforming the AST.

Now open `helloworld/spoofax.cfg` again and register the tasks by adding:

```cfg
task-def mb.helloworld.task.HelloWorldReplaceWorlds
let showReplaceWorlds = task-def mb.helloworld.task.HelloWorldShowReplaceWorlds
```

Then add a command for it by adding:

```cfg
let showReplaceWorldsCommand = command-def {
  task-def = showReplaceWorlds
  display-name = "Show result of replace worlds transformation"
  description = "Shows the resulting AST of the replace world transformation"
  parameters = [
    file = parameter {
      type = java mb.resource.ResourceKey
      argument-providers = [Context(File)]
    }
  ]
}
```

Finally, add menu items for the command by adding:

```cfg
editor-context-menu [
  menu "Debug" [
    command-action {
      command-def = showReplaceWorldsCommand
      execution-type = Once
    }
    command-action {
      command-def = showReplaceWorldsCommand
      execution-type = Continuous
    }
  ]
]
resource-context-menu [
  menu "Debug" [
    command-action {
      command-def = showReplaceWorldsCommand
      execution-type = Once
      required-resource-types = [File]
    }
  ]
]
```

Build the project so that we can test our changes.

!!! bug
    Building the project at this point will result in an error. This can be worked around by first cleaning the project by choosing <span class="guilabel">Project ‣ Clean</span> from the main menu. This bug will be fixed in a future version.

Test the command similarly to testing the "Show parsed AST" command.
