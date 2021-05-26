# Creating a language project

--8<-- "docs/_include/_all.md"

This tutorial gets you started with language development by creating a language project and changing various aspects of the language. First follow the [installation tutorial](install.md) if you haven't done so yet.

## Creating a new project

In Eclipse, open the new project dialog by choosing <span class="guilabel">File ‣ New ‣ Project</span> from the main menu.
In the new project dialog, select <span class="guilabel">Spoofax LWB ‣ Spoofax language project</span> and press <span class="guilabel">Next</span>.
In this wizard, you can customize the various names your language will use.
However, for the purpose of this tutorial, fill in `HelloWorld` as the name of the project, which will automatically fill in the other elements with defaults.
Then press <span class="guilabel">Finish</span> to create the project.
There should now be a project named `helloworld` in the <span class="guilabel">Package Explorer</span>.

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
To do that, we will write a *task definition* that produces the AST of a program by parsing it, we will add a *command definition* for that task, and finally we will bind the command to a *menu item* so that we can execute the command.

### Creating the task definition

A task definition is a piece of code that take some input, may read from or write to files, run and get the result of other tasks, and produce some output.
Task definitions come from [PIE](https://github.com/metaborg/pie), a framework for developing composable, incremental, correct, and expressive pipelines and build scripts.

All user interaction, pipelines, and builds in Spoofax 3 are composed of task definitions.
So whenever you want to perform a command, present feedback to the user, or compile your language, you will need to write a task definition for it.
For brevity, we usually just refer to a "task definition" by "task".

For PIE to be able to incrementally execute your task, you must make your dependencies explicit.
That is, dependencies to files and other tasks must be made explicit.
However, because PIE supports *dynamic dependencies*, those dependencies are made *while the build script is executing*.
A full tutorial on PIE is outside the scope of this tutorial, but we will implement several tasks in this tutorial, explain the PIE concepts, and how Spoofax 3 uses these concepts.

A task definition is written as a class in Java, and needs to adhere to a certain interface.
Let's start by creating the class for this task.
First, right-click the `helloworld/src/main/java` directory and choose <span class="guilabel">New ‣ Package</span>, replace the name with `mb.helloworld.task`, and press <span class="guilabel">Finish</span>.
Then, right-click the `mb.helloworld.task` package we just created and choose <span class="guilabel">New ‣ Class</span> and fill in `HelloWorldShowParsedAst` as name, then press <span class="guilabel">Finish</span>.
Replace the entire Java file with the following code:

```{ .java .annotate linenums="1" }
package mb.helloworld.task;

import java.io.Serializable;
import java.util.Objects;

import javax.inject.Inject;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.common.result.Result;
import mb.helloworld.HelloWorldClassLoaderResources;
import mb.helloworld.HelloWorldScope;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.stratego.common.StrategoUtil;

@HelloWorldScope // (10)
public class HelloWorldShowParsedAst implements TaskDef<HelloWorldShowParsedAst.Args, CommandFeedback> { // (1)
    public static class Args implements Serializable { // (2)
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


    @Override
    public CommandFeedback exec(ExecContext context, Args args) throws Exception { // (3)
        context.require(classloaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile()); // (4)
        final ResourceKey file = args.file;
        final Result<IStrategoTerm, JSGLR1ParseException> astResult = context.require(parse.inputBuilder().withFile(file).buildAstSupplier()); // (5)
        return astResult.mapOrElse( // (6)
            ast -> CommandFeedback.of(ShowFeedback.showText(StrategoUtil.toString(ast), "Parsed AST for '" + file + "'")),
            e -> CommandFeedback.ofTryExtractMessagesFrom(e, file)
        );
    }

    @Override
    public String getId() { // (7)
        return getClass().getName();
    }


    // (8)
    private final HelloWorldClassLoaderResources classloaderResources;
    private final HelloWorldParse parse;

    @Inject // (9)
    public HelloWorldShowParsedAst(HelloWorldClassLoaderResources classloaderResources, HelloWorldParse parse) {
        this.classloaderResources = classloaderResources;
        this.parse = parse;
    }
}
```

We explain this class with numbered annotations in the above Java source file:

 1. This class implements `#!java TaskDef`, which is an interface from PIE which a class must implement to be a task definition. Task definitions have an input and output type defined by the first and second generic argument. To execute this task, an object of the input type is required, and once it is done executing, it must return an object of the output type.

    In this concrete case, the input type is `#!java HelloWorldShowParsedAst.Args` which is a nested data class defined at (2). The output is `#!java CommandFeedback` which is a type defined by Spoofax for providing feedback back to the user when executing a command. All tasks that are executed through commands must return a `#!java CommandFeedback` object.

 2. A nested data class encapsulating the input to this task. In this case, we want this task to take the `file` we are going to show the AST of as input. Even though we only take one argument as input, we must encapsulate it in a class due to the way commands in Spoofax work.

    This data class must implement `#!java Serializable` because it is used as an input to a task. In order for PIE to incrementalize tasks across JVM restarts, it must be able to serialize the input (and output) objects to disk. Furthermore, this class must be immutable, because PIE caches input (and output) objects, and this caching would be inconsistent if the class is mutable. This class is immutable by storing the file in a `#!java final` field which is set in the constructor (and the `#!java ResourceKey` class is immutable as well).

    Spoofax and PIE abstract over files with *resources*. A resource is some externally managed mutable state, with a (immutable and serializable) *key* which can be used to identify, read, and write to that resource. Such a key is represented by a `#!java ResourceKey`.

    Finally, this data class must implement `#!java equals` and `#!java hashCode` according to the data in the class because PIE uses these methods to identify tasks according to their input, which in turn is used for caching. A `#!java toString` implementation is also recommended for debugging.

 3. The `exec` method which is called when the task is executed. It takes an `#!java ExecContext` as input, which is used to tell PIE about dependencies to files, and can be used to execute and get the result of another task (implicitly creating a dependency to that task). It also takes the input type `Args` as input, and must return a `CommandFeedback`.

 4. For sound incrementality, we want to re-execute this task when we make changes to this class. Therefore, we want to make a *self-dependency*. That is, we want to make a file dependency to the Java class file that is compiled from this Java source file. The `#!java classloaderResources` object (defined below at (8)) is used to get the class or JAR file of the current class. This resource is passed to `#!java context.require` to tell PIE that this task depends on that file.

    We pass in `#!java ResourceStampers.hashFile()` as the second argument, which indicates that we want to use the hash of the class file to detect changes, instead of the last modified date which is used by default. It is recommended to use hashes for dependencies to generated/compiled files, as compiled files are sometimes recompiled without changes, which changes the modified date but not the hash, leading to better incrementality.

 5. To show the AST we must parse the input file, and in order to do that we must call a task which performs the parsing. Whereas (4) uses `#!java context.require` to create a dependency to a *file*, we use `#!java context.require` here to create a dependency to the *task* that does the parsing, and get the output of that task. As input to `#!java context.require` we pass `#!java parse.inputBuilder().withFile(file).buildAstSupplier()`, which uses the builder pattern to create an input for the `parse` task and then extracts the AST from the output.

    Internally, the `#!java parse` task creates a dependency to the `#!java file` we pass into it. We depend on the `#!java parse` task. Therefore, when the `#!java file` changes, PIE re-executes the `#!java parse` task, and then re-executes this task if the output of the `#!java parse` task is different. Thereby, PIE incrementally executes your task without having to incrementalize it yourself.

    The output of the `#!java parse` task is `#!java Result<IStrategoTerm, JSGLR1ParseException>` which is a [*result type*](https://en.wikipedia.org/wiki/Result_type) which is either a `#!java IStrategoTerm` representing the AST of the file if parsing succeeds, or a `#!java JSGLR1ParseException` when parsing fails.

    Instead of throwing exceptions, we use result types (akin to `Either` in Haskell or `Result` in Rust) to model the possibility of failure with values. We do this to make it possible to work with failures in PIE tasks. In PIE, throwing an exception signifies an unrecoverable error and cancels the entire pipeline. However, using failures as values works normally.

 6. Now that we have the result of parsing, we can turn it into a `#!java CommandFeedback` object. We use `#!java mapOrElse` of `#!java Result` to map the result to a `#!java CommandFeedback` differently depending on whether parsing succeeded or failed.

    If parsing succeeded, we show the AST as text to the user with `#!java CommandFeedback.of(ShowFeedback.showText(...))` with the first argument providing the text, and the second argument providing the title. The IDE then shows this as a textual editor.

    If parsing failed, we present the parse error messages as feedback to the user with `#!java CommandFeedback.ofTryExtractMessagesFrom`.

 7. Finally, PIE needs to be able to identify this task definition. That is done by this `getId` method that returns a unique identifier. This can almost always be implemented using `#!java getClass().getName()` which returns the fully qualified name of this class.

 8. Spoofax uses [dependency injection](https://en.wikipedia.org/wiki/Dependency_injection) to inject required services, tasks, and other objects into the objects of your classes. The `#!java classloaderResources` object used in (4) is of type `#!java HelloWorldClassLoaderResources` which is class that Spoofax generates for your language. Similarly, the `#!java parse` object used in (5) is of type `#!java HelloWorldParse` which is a task definition that Spoofax generates for you. We store these as fields of this class.

    Note that dependency injection, and file/task dependencies in PIE, are two completely separate things.

 9. These fields are set using [constructor injection](https://en.wikipedia.org/wiki/Dependency_injection#Constructor_injection) in the single constructor of this class marked with `#!java @Inject`. The dependency injection framework that Spoofax uses (the [Dagger](https://dagger.dev/dev-guide/) framework) will then instantiate your class with instances of the dependencies.

10. Finally, we must tell the dependency injection framework to which scope instances of this class belongs. We annotate the class with `#!java @HelloWorldScope` which is a scope annotation that Spoofax generates for you. This is mainly used to differentiate between different languages when multiple languages are composed, which we do not do in this tutorial, but is required nonetheless.

### Registering the task definition

We must register this task in order for Spoofax to know about it.
Open the CFG `helloworld/spoofax.cfg` file.
The CFG meta-language is a configuration language where we configure and glue together the various aspects of your language.
Add the following configuration to the end of the file:

```cfg
let showParsedAst = task-def mb.helloworld.task.HelloWorldShowParsedAst
```

This registers the task definition class that we just created, and makes it available under the `showParsedAst` name in the configuration.

!!! warning
    Spoofax assumes that this class implements `TaskDef`. This is not checked as part of this configuration. Faults will lead to Java compile errors.

### Creating the command

To create the command, add the following configuration to the end of the `spoofax.cfg` file:

```{ .cfg .annotate }
let showParsedAstCommand = command-def {
  type = java mb.helloworld.command.HelloWorldShowParsedAstCommand // (1)
  task-def = showParsedAst // (2)
  args-type = java mb.helloworld.task.HelloWorldShowParsedAst.Args // (3)
  display-name = "Show parsed AST" // (4)
  description = "Shows the parsed AST" // (5)
  supported-execution-types = [Once, Continuous] // (6)
  parameters = [ // (7)
    file = parameter { (7a)
      type = java mb.resource.ResourceKey (7b)
      required = true (7c)
      argument-providers = [Context(File)] (7d)
    }
  ]
}
```

1. Spoofax generates a Java class implementing the command boilerplate for you. This is the fully qualified Java type we want this command to have. Can be omitted to generate a type based on the name of the task definition.
2. The task definition that the command will execute, which is the `#!cfg showParsedAst` we defined earlier.
3. The fully qualified Java type of the argument class. Can be omitted if the argument class is a nested class named `Args` of the task definition.
4. The display name of the command.
5. The optional description of the command.
6. The optional supported execution types of the command. `#!cfg Once` indicates a one-shot command, while `#!cfg Continuous` indicates a command that is executed every time the source file changes. Defaults to `#!cfg [Once, Continuous]`.
7. The description of the parameters of the command:
    1. The name of the parameter.
    2. The fully qualified Java of the type of the parameter. This must match the type we used in the `#!java HelloWorldShowParsedAst.Args` class before.
    3. Whether the parameter is required. Defaults to `#!cfg true`.
    4. Argument providers for this parameter that attempt to automatically provide a fitting argument. When providing an argument fails, the next argument provider in the list will be attempted.

        Because this argument is a file, we want to try to infer the file from context, so we use `#!cfg Context(File)`. When we execute this command on a "Hello world" file in the IDE, Spoofax will automatically infer that file as the argument for the parameter.

        Currently, Spoofax does not support running commands in the IDE without an argument provider, so a working argument provider is currently required.

!!! warning
    Spoofax assumes that: a) the task definition's input type is the one defined at (3), b) the output type is `CommandFeedback`, and c) that the argument type has a constructor covering exactly the parameters from (7). This is not checked as part of this configuration. Faults will lead to Java compile errors.

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

### Adding the menu item

To add the menu item, add the following configuration to the end of the `spoofax.cfg` file:

```{ .cfg .annotate }
editor-context-menu [ // (1)
  menu "Debug" [ // (2)
    command-action { // (3)
      command-def = showParsedAstCommand // (3a)
      execution-type = Once // (3b)
    }
    command-action {
      command-def = showParsedAstCommand
      execution-type = Continuous
    }
  ]
]
resource-context-menu [ // (4)
  menu "Debug" [
    command-action {
      command-def = showParsedAstCommand
      execution-type = Once
      required-resource-types = [File] // (5)
    }
  ]
]
```

 1. An `#!cfg editor-context-menu` section adds menu items to the editor context menu. That is, the menu that pops up when you right-click inside an editor for your language. There is also a `#!cfg main-menu` section for adding menu items to the main menu when an editor for your language has focus. If no `#!cfg main-menu` section is defined, the main menu will take all menu items from `#!cfg editor-context-menu`.
 2. A nested menu with name `#!cfg "Debug"`.
 3. An action menu item that executes a command.
    1. The command to execute.
    2. How the command should be executed. `#!cfg Once` to execute it once, `#!cfg Continuous` to continuously execute the command when the source file changes.
 4. A `#!cfg resource-context-menu` section adds menu items to the context menu of the resource browser. That is, the menu that pops up when you right-click a file of your language.
 5. For menu items inside a `#!cfg resource-context-menu` to show up, they must specify on what kind of resource types they are shown. In this case, we want the command to show up for files of our language, so we choose `[File]`.

Build the project so that we can test our changes.
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

```{ .java .annotate linenums="1" }
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
  public HelloWorldReplaceWorlds( // 1
    HelloWorldClassLoaderResources classloaderResources,
    HelloWorldGetStrategoRuntimeProvider getStrategoRuntimeProvider
  ) {
    super(getStrategoRuntimeProvider, "replace-worlds"); // 2
    this.classloaderResources = classloaderResources;
  }

  @Override public String getId() { // 3
    return getClass().getName();
  }

  @Override protected void createDependencies(ExecContext context) throws IOException { // 4
    context.require(classloaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
  }
}

```

This task extends `#!java AstStrategoTransformTaskDef` which is a convenient abstract class for creating tasks that run Stratego transformations by implementing a constructor and a couple of methods:

 1. The constructor should inject `#!java HelloWorldClassLoaderResources` which we again will use to create a self-dependency, and `#!java HelloWorldGetStrategoRuntimeProvider` which is a task that Spoofax generates for your language, which provides a Stratego runtime to execute strategies with.
 2. The `#!java HelloWorldGetStrategoRuntimeProvider` instance is provided to the superclass constructor, along with the strategy that we want this task to execute, which is `#!java "replace-worlds"`.
 3. We override `#!java getId` of `#!java TaskDef` again to give this task a unique identifier.
 4. We override `#!java createDependencies` of `#!java AstStrategoTransformTaskDef` to create a self-dependency.

Then create the `HelloWorldShowReplaceWorlds` class and replace the entire Java file with:

```{ .java .annotate linenums="1" }
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
  display-name = "Replace world with hello"
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
  menu "Transform" [
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
  menu "Transform" [
    command-action {
      command-def = showReplaceWorldsCommand
      execution-type = Once
      required-resource-types = [File]
    }
  ]
]
```

Build the project so that we can test our changes.
Test the command similarly to testing the "Show parsed AST" command.
