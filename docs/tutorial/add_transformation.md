# Adding a transformation

Finally, we will define a transformation for our language and add a task, command-tasks, command, and menu item for it.
Open the main Stratego file `helloworld/src/main.str`.
[Stratego](https://www.spoofax.dev/references/stratego/). is a meta-language for defining term (AST) transformations through rewrite rules.
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

??? note "`src/main.str` full contents"
```stratego
module main

    imports

      statixruntime
      statix/api
      injections/-
      signatures/-

    rules // Analysis

      pre-analyze  = explicate-injections-helloworld-Start
      post-analyze = implicate-injections-helloworld-Start

      editor-analyze = stx-editor-analyze(pre-analyze, post-analyze|"main", "programOk")

    rules

      replace-world: Hello() -> Hello()
      replace-world: World() -> Hello()
      replace-worlds = topdown(try(replace-world))
    ```


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
    context.require(classloaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());
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
import mb.aterm.common.TermToString;

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
        context.require(classloaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());
        final ResourceKey file = args.file;
        return context.require(replaceWorlds, parse.inputBuilder().withFile(file).buildAstSupplier()).mapOrElse(
            ast -> CommandFeedback.of(ShowFeedback.showText(TermToString.toString(ast), "Replaced World()s with Hello()s for '" + file + "'")),
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
      argument-providers = [Context(ReadableResource)]
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

[comment]: <> (## Adding a debugging command)

[comment]: <> (It can be quite handy to look at the AST of a program that the parser of the language produces as a debugging tool.)

[comment]: <> (To do that, we will write a *task definition* that produces the AST of a program by parsing it, we will add a *command definition* for that task, and finally we will bind the command to a *menu item* so that we can execute the command.)

[comment]: <> (### Creating the task definition)

[comment]: <> (A task definition is a piece of code that take some input, may read from or write to files, run and get the result of other tasks, and produce some output.)

[comment]: <> (Task definitions come from [PIE]&#40;https://github.com/metaborg/pie&#41;, a framework for developing composable, incremental, correct, and expressive pipelines and build scripts.)

[comment]: <> (All user interaction, pipelines, and builds in Spoofax 3 are composed of task definitions.)

[comment]: <> (So whenever you want to perform a command, present feedback to the user, or compile your language, you will need to write a task definition for it.)

[comment]: <> (For brevity, we usually just refer to a "task definition" by "task".)

[comment]: <> (For PIE to be able to incrementally execute your task, you must make your dependencies explicit.)

[comment]: <> (That is, dependencies to files and other tasks must be made explicit.)

[comment]: <> (However, because PIE supports *dynamic dependencies*, those dependencies are made *while the build script is executing*.)

[comment]: <> (A full tutorial on PIE is outside the scope of this tutorial, but we will implement several tasks in this tutorial, explain the PIE concepts, and how Spoofax 3 uses these concepts.)

[comment]: <> (A task definition is written as a class in Java, and needs to adhere to a certain interface.)

[comment]: <> (Let's start by creating the class for this task.)

[comment]: <> (First, right-click the `helloworld/src/main/java` directory and choose <span class="guilabel">New ‣ Package</span>, replace the name with `mb.helloworld.task`, and press <span class="guilabel">Finish</span>.)

[comment]: <> (Then, right-click the `mb.helloworld.task` package we just created and choose <span class="guilabel">New ‣ Class</span> and fill in `HelloWorldShowParsedAst` as name, then press <span class="guilabel">Finish</span>.)

[comment]: <> (Replace the entire Java file with the following code:)

[comment]: <> (```{ .java .annotate linenums="1" })

[comment]: <> (package mb.helloworld.task;)

[comment]: <> (import java.io.Serializable;)

[comment]: <> (import java.util.Objects;)

[comment]: <> (import javax.inject.Inject;)

[comment]: <> (import org.checkerframework.checker.nullness.qual.Nullable;)

[comment]: <> (import org.spoofax.interpreter.terms.IStrategoTerm;)

[comment]: <> (import mb.common.result.Result;)

[comment]: <> (import mb.helloworld.HelloWorldClassLoaderResources;)

[comment]: <> (import mb.helloworld.HelloWorldScope;)

[comment]: <> (import mb.jsglr.common.JsglrParseException;)

[comment]: <> (import mb.pie.api.ExecContext;)

[comment]: <> (import mb.pie.api.TaskDef;)

[comment]: <> (import mb.pie.api.stamp.resource.ResourceStampers;)

[comment]: <> (import mb.resource.ResourceKey;)

[comment]: <> (import mb.spoofax.core.language.command.CommandFeedback;)

[comment]: <> (import mb.spoofax.core.language.command.ShowFeedback;)

[comment]: <> (import mb.aterm.common.TermToString;)

[comment]: <> (@HelloWorldScope // &#40;10&#41;)

[comment]: <> (public class HelloWorldShowParsedAst implements TaskDef<HelloWorldShowParsedAst.Args, CommandFeedback> { // &#40;1&#41;)

[comment]: <> (    public static class Args implements Serializable { // &#40;2&#41;)

[comment]: <> (        private static final long serialVersionUID = 1L;)

[comment]: <> (        public final ResourceKey file;)

[comment]: <> (        public Args&#40;ResourceKey file&#41; {)

[comment]: <> (            this.file = file;)

[comment]: <> (        })

[comment]: <> (        @Override)

[comment]: <> (        public boolean equals&#40;@Nullable Object o&#41; {)

[comment]: <> (            if&#40;this == o&#41; return true;)

[comment]: <> (            if&#40;o == null || getClass&#40;&#41; != o.getClass&#40;&#41;&#41; return false;)

[comment]: <> (            final Args args = &#40;Args&#41;o;)

[comment]: <> (            return file.equals&#40;args.file&#41;;)

[comment]: <> (        })

[comment]: <> (        @Override)

[comment]: <> (        public int hashCode&#40;&#41; {)

[comment]: <> (            return Objects.hash&#40;file&#41;;)

[comment]: <> (        })

[comment]: <> (        @Override)

[comment]: <> (        public String toString&#40;&#41; {)

[comment]: <> (            return "Args{" + "file=" + file + '}';)

[comment]: <> (        })

[comment]: <> (    })


[comment]: <> (    @Override)

[comment]: <> (    public CommandFeedback exec&#40;ExecContext context, Args args&#41; throws Exception { // &#40;3&#41;)

[comment]: <> (        context.require&#40;classloaderResources.tryGetAsNativeResource&#40;getClass&#40;&#41;&#41;, ResourceStampers.hashFile&#40;&#41;&#41;; // &#40;4&#41;)

[comment]: <> (        final ResourceKey file = args.file;)

[comment]: <> (        final Result<IStrategoTerm, JsglrParseException> astResult = context.require&#40;parse.inputBuilder&#40;&#41;.withFile&#40;file&#41;.buildAstSupplier&#40;&#41;&#41;; // &#40;5&#41;)

[comment]: <> (        return astResult.mapOrElse&#40; // &#40;6&#41;)

[comment]: <> (            ast -> CommandFeedback.of&#40;ShowFeedback.showText&#40;TermToString.toString&#40;ast&#41;, "Parsed AST for '" + file + "'"&#41;&#41;,)

[comment]: <> (            e -> CommandFeedback.ofTryExtractMessagesFrom&#40;e, file&#41;)

[comment]: <> (        &#41;;)

[comment]: <> (    })

[comment]: <> (    @Override)

[comment]: <> (    public String getId&#40;&#41; { // &#40;7&#41;)

[comment]: <> (        return getClass&#40;&#41;.getName&#40;&#41;;)

[comment]: <> (    })


[comment]: <> (    // &#40;8&#41;)

[comment]: <> (    private final HelloWorldClassLoaderResources classloaderResources;)

[comment]: <> (    private final HelloWorldParse parse;)

[comment]: <> (    @Inject // &#40;9&#41;)

[comment]: <> (    public HelloWorldShowParsedAst&#40;HelloWorldClassLoaderResources classloaderResources, HelloWorldParse parse&#41; {)

[comment]: <> (        this.classloaderResources = classloaderResources;)

[comment]: <> (        this.parse = parse;)

[comment]: <> (    })

[comment]: <> (})

[comment]: <> (```)

[comment]: <> (We explain this class with numbered annotations in the above Java source file:)

[comment]: <> (1. This class implements `#!java TaskDef`, which is an interface from PIE which a class must implement to be a task definition. Task definitions have an input and output type defined by the first and second generic argument. To execute this task, an object of the input type is required, and once it is done executing, it must return an object of the output type.)

[comment]: <> (   In this concrete case, the input type is `#!java HelloWorldShowParsedAst.Args` which is a nested data class defined at &#40;2&#41;. The output is `#!java CommandFeedback` which is a type defined by Spoofax for providing feedback back to the user when executing a command. All tasks that are executed through commands must return a `#!java CommandFeedback` object.)

[comment]: <> (2. A nested data class encapsulating the input to this task. In this case, we want this task to take the `file` we are going to show the AST of as input. Even though we only take one argument as input, we must encapsulate it in a class due to the way commands in Spoofax work.)

[comment]: <> (   This data class must implement `#!java Serializable` because it is used as an input to a task. In order for PIE to incrementalize tasks across JVM restarts, it must be able to serialize the input &#40;and output&#41; objects to disk. Furthermore, this class must be immutable, because PIE caches input &#40;and output&#41; objects, and this caching would be inconsistent if the class is mutable. This class is immutable by storing the file in a `#!java final` field which is set in the constructor &#40;and the `#!java ResourceKey` class is immutable as well&#41;.)

[comment]: <> (   Spoofax and PIE abstract over files with *resources*. A resource is some externally managed mutable state, with a &#40;immutable and serializable&#41; *key* which can be used to identify, read, and write to that resource. Such a key is represented by a `#!java ResourceKey`.)

[comment]: <> (   Finally, this data class must implement `#!java equals` and `#!java hashCode` according to the data in the class because PIE uses these methods to identify tasks according to their input, which in turn is used for caching. A `#!java toString` implementation is also recommended for debugging.)

[comment]: <> (3. The `exec` method which is called when the task is executed. It takes an `#!java ExecContext` as input, which is used to tell PIE about dependencies to files, and can be used to execute and get the result of another task &#40;implicitly creating a dependency to that task&#41;. It also takes the input type `Args` as input, and must return a `CommandFeedback`.)

[comment]: <> (4. For sound incrementality, we want to re-execute this task when we make changes to this class. Therefore, we want to make a *self-dependency*. That is, we want to make a file dependency to the Java class file that is compiled from this Java source file. The `#!java classloaderResources` object &#40;defined below at &#40;8&#41;&#41; is used to get the class or JAR file of the current class. This resource is passed to `#!java context.require` to tell PIE that this task depends on that file.)

[comment]: <> (   We pass in `#!java ResourceStampers.hashFile&#40;&#41;` as the second argument, which indicates that we want to use the hash of the class file to detect changes, instead of the last modified date which is used by default. It is recommended to use hashes for dependencies to generated/compiled files, as compiled files are sometimes recompiled without changes, which changes the modified date but not the hash, leading to better incrementality.)

[comment]: <> (5. To show the AST we must parse the input file, and in order to do that we must call a task which performs the parsing. Whereas &#40;4&#41; uses `#!java context.require` to create a dependency to a *file*, we use `#!java context.require` here to create a dependency to the *task* that does the parsing, and get the output of that task. As input to `#!java context.require` we pass `#!java parse.inputBuilder&#40;&#41;.withFile&#40;file&#41;.buildAstSupplier&#40;&#41;`, which uses the builder pattern to create an input for the `parse` task and then extracts the AST from the output.)

[comment]: <> (   Internally, the `#!java parse` task creates a dependency to the `#!java file` we pass into it. We depend on the `#!java parse` task. Therefore, when the `#!java file` changes, PIE re-executes the `#!java parse` task, and then re-executes this task if the output of the `#!java parse` task is different. Thereby, PIE incrementally executes your task without having to incrementalize it yourself.)

[comment]: <> (   The output of the `#!java parse` task is `#!java Result<IStrategoTerm, JSGLR1ParseException>` which is a [*result type*]&#40;https://en.wikipedia.org/wiki/Result_type&#41; which is either a `#!java IStrategoTerm` representing the AST of the file if parsing succeeds, or a `#!java JSGLR1ParseException` when parsing fails.)

[comment]: <> (   Instead of throwing exceptions, we use result types &#40;akin to `Either` in Haskell or `Result` in Rust&#41; to model the possibility of failure with values. We do this to make it possible to work with failures in PIE tasks. In PIE, throwing an exception signifies an unrecoverable error and cancels the entire pipeline. However, using failures as values works normally.)

[comment]: <> (6. Now that we have the result of parsing, we can turn it into a `#!java CommandFeedback` object. We use `#!java mapOrElse` of `#!java Result` to map the result to a `#!java CommandFeedback` differently depending on whether parsing succeeded or failed.)

[comment]: <> (   If parsing succeeded, we show the AST as text to the user with `#!java CommandFeedback.of&#40;ShowFeedback.showText&#40;...&#41;&#41;` with the first argument providing the text, and the second argument providing the title. The IDE then shows this as a textual editor.)

[comment]: <> (   If parsing failed, we present the parse error messages as feedback to the user with `#!java CommandFeedback.ofTryExtractMessagesFrom`.)

[comment]: <> (7. Finally, PIE needs to be able to identify this task definition. That is done by this `getId` method that returns a unique identifier. This can almost always be implemented using `#!java getClass&#40;&#41;.getName&#40;&#41;` which returns the fully qualified name of this class.)

[comment]: <> (8. Spoofax uses [dependency injection]&#40;https://en.wikipedia.org/wiki/Dependency_injection&#41; to inject required services, tasks, and other objects into the objects of your classes. The `#!java classloaderResources` object used in &#40;4&#41; is of type `#!java HelloWorldClassLoaderResources` which is class that Spoofax generates for your language. Similarly, the `#!java parse` object used in &#40;5&#41; is of type `#!java HelloWorldParse` which is a task definition that Spoofax generates for you. We store these as fields of this class.)

[comment]: <> (   Note that dependency injection, and file/task dependencies in PIE, are two completely separate things.)

[comment]: <> (9. These fields are set using [constructor injection]&#40;https://en.wikipedia.org/wiki/Dependency_injection#Constructor_injection&#41; in the single constructor of this class marked with `#!java @Inject`. The dependency injection framework that Spoofax uses &#40;the [Dagger]&#40;https://dagger.dev/dev-guide/&#41; framework&#41; will then instantiate your class with instances of the dependencies.)

[comment]: <> (10. Finally, we must tell the dependency injection framework to which scope instances of this class belongs. We annotate the class with `#!java @HelloWorldScope` which is a scope annotation that Spoofax generates for you. This is mainly used to differentiate between different languages when multiple languages are composed, which we do not do in this tutorial, but is required nonetheless.)

[comment]: <> (### Registering the task definition)

[comment]: <> (We must register this task in order for Spoofax to know about it.)

[comment]: <> (Open the CFG `helloworld/spoofax.cfg` file.)

[comment]: <> (The [CFG]&#40;../reference/configuration.md&#41; meta-language is a configuration language where we configure and glue together the various aspects of your language.)

[comment]: <> (Add the following configuration to the end of the file:)

[comment]: <> (```cfg)

[comment]: <> (let showParsedAst = task-def mb.helloworld.task.HelloWorldShowParsedAst)

[comment]: <> (```)

[comment]: <> (This registers the task definition class that we just created, and makes it available under the `showParsedAst` name in the configuration.)

[comment]: <> (!!! warning)

[comment]: <> (Spoofax assumes that this class implements `TaskDef`. This is not checked as part of this configuration. Faults will lead to Java compile errors.)

[comment]: <> (### Creating the command)

[comment]: <> (To create the command, add the following configuration to the end of the `spoofax.cfg` file:)

[comment]: <> (```{ .cfg .annotate })

[comment]: <> (let showParsedAstCommand = command-def {)

[comment]: <> (  type = java mb.helloworld.command.HelloWorldShowParsedAstCommand // &#40;1&#41;)

[comment]: <> (  task-def = showParsedAst // &#40;2&#41;)

[comment]: <> (  args-type = java mb.helloworld.task.HelloWorldShowParsedAst.Args // &#40;3&#41;)

[comment]: <> (  display-name = "Show parsed AST" // &#40;4&#41;)

[comment]: <> (  description = "Shows the parsed AST" // &#40;5&#41;)

[comment]: <> (  supported-execution-types = [Once, Continuous] // &#40;6&#41;)

[comment]: <> (  parameters = [ // &#40;7&#41;)

[comment]: <> (    file = parameter { // &#40;7a&#41;)

[comment]: <> (      type = java mb.resource.ResourceKey // &#40;7b&#41;)

[comment]: <> (      required = true // &#40;7c&#41;)

[comment]: <> (      argument-providers = [Context&#40;ReadableResource&#41;] // &#40;7d&#41;)

[comment]: <> (    })

[comment]: <> (  ])

[comment]: <> (})

[comment]: <> (```)

[comment]: <> (1. Spoofax generates a Java class implementing the command boilerplate for you. This is the fully qualified Java type we want this command to have. Can be omitted to generate a type based on the name of the task definition.)

[comment]: <> (2. The task definition that the command will execute, which is the `#!cfg showParsedAst` we defined earlier.)

[comment]: <> (3. The fully qualified Java type of the argument class. Can be omitted if the argument class is a nested class named `Args` of the task definition.)

[comment]: <> (4. The display name of the command.)

[comment]: <> (5. The optional description of the command.)

[comment]: <> (6. The optional supported execution types of the command. `#!cfg Once` indicates a one-shot command, while `#!cfg Continuous` indicates a command that is executed every time the source file changes. Defaults to `#!cfg [Once, Continuous]`.)

[comment]: <> (7. The description of the parameters of the command:)

[comment]: <> (  1. The name of the parameter.)

[comment]: <> (  2. The fully qualified Java of the type of the parameter. This must match the type we used in the `#!java HelloWorldShowParsedAst.Args` class before.)

[comment]: <> (  3. Whether the parameter is required. Defaults to `#!cfg true`.)

[comment]: <> (  4. Argument providers for this parameter that attempt to automatically provide a fitting argument. When providing an argument fails, the next argument provider in the list will be attempted.)

[comment]: <> (     Because this argument is a `ResourceKey` that should point to a readable resource, we want to try to infer the file from context, so we use `#!cfg Context&#40;ReadableResource&#41;`. When we execute this command on a "Hello world" file in the IDE, Spoofax will automatically infer that file as the argument for the parameter, because the file is a readable resource.)

[comment]: <> (     Currently, Spoofax does not support running commands in the IDE without an argument provider, so a working argument provider is currently required.)

[comment]: <> (!!! warning)

[comment]: <> (Spoofax assumes that: a&#41; the task definition's input type is the one defined at &#40;3&#41;, b&#41; the output type is `CommandFeedback`, and c&#41; that the argument type has a constructor covering exactly the parameters from &#40;7&#41;. This is not checked as part of this configuration. Faults will lead to Java compile errors.)

[comment]: <> (Some properties set above are set to their conventional &#40;default&#41; value, or are optional, so we can leave them out. Replace the command definition with the following code:)

[comment]: <> (```cfg)

[comment]: <> (let showParsedAstCommand = command-def {)

[comment]: <> (  task-def = showParsedAst)

[comment]: <> (  display-name = "Show parsed AST")

[comment]: <> (  parameters = [)

[comment]: <> (    file = parameter {)

[comment]: <> (      type = java mb.resource.ResourceKey)

[comment]: <> (      argument-providers = [Context&#40;ReadableResource&#41;])

[comment]: <> (    })

[comment]: <> (  ])

[comment]: <> (})

[comment]: <> (```)

[comment]: <> (### Adding the menu item)

[comment]: <> (To add the menu item, add the following configuration to the end of the `spoofax.cfg` file:)

[comment]: <> (```{ .cfg .annotate })

[comment]: <> (editor-context-menu [ // &#40;1&#41;)

[comment]: <> (  menu "Debug" [ // &#40;2&#41;)

[comment]: <> (    command-action { // &#40;3&#41;)

[comment]: <> (      command-def = showParsedAstCommand // &#40;3a&#41;)

[comment]: <> (      execution-type = Once // &#40;3b&#41;)

[comment]: <> (    })

[comment]: <> (    command-action {)

[comment]: <> (      command-def = showParsedAstCommand)

[comment]: <> (      execution-type = Continuous)

[comment]: <> (    })

[comment]: <> (  ])

[comment]: <> (])

[comment]: <> (resource-context-menu [ // &#40;4&#41;)

[comment]: <> (  menu "Debug" [)

[comment]: <> (    command-action {)

[comment]: <> (      command-def = showParsedAstCommand)

[comment]: <> (      execution-type = Once)

[comment]: <> (      required-resource-types = [File] // &#40;5&#41;)

[comment]: <> (    })

[comment]: <> (  ])

[comment]: <> (])

[comment]: <> (```)

[comment]: <> (1. An `#!cfg editor-context-menu` section adds menu items to the editor context menu. That is, the menu that pops up when you right-click inside an editor for your language. There is also a `#!cfg main-menu` section for adding menu items to the main menu when an editor for your language has focus. If no `#!cfg main-menu` section is defined, the main menu will take all menu items from `#!cfg editor-context-menu`.)

[comment]: <> (2. A nested menu with name `#!cfg "Debug"`.)

[comment]: <> (3. An action menu item that executes a command.)

[comment]: <> (  1. The command to execute.)

[comment]: <> (  2. How the command should be executed. `#!cfg Once` to execute it once, `#!cfg Continuous` to continuously execute the command when the source file changes.)

[comment]: <> (4. A `#!cfg resource-context-menu` section adds menu items to the context menu of the resource browser. That is, the menu that pops up when you right-click a file of your language.)

[comment]: <> (5. For menu items inside a `#!cfg resource-context-menu` to show up, they must specify on what kind of resource types they are shown. In this case, we want the command to show up when files of our language are selected in when the resource context menu pops up, so we choose `[File]`.)

[comment]: <> (Build the project so that we can test our changes.)

[comment]: <> (Open the `example1.hel` file and right-click inside the editor area to open the context menu.)

[comment]: <> (From the editor context menu, select <span class="guilabel">HelloWorld ‣ Debug ‣ Show parsed AST</span>.)

[comment]: <> (A new editor will pop up with the result of the command, showing the AST of your example file.)

[comment]: <> (Close the result and now run <span class="guilabel">HelloWorld ‣ Debug ‣ Show parsed AST &#40;continuous&#41;</span>.)

[comment]: <> (Drag the resulting editor to the side and edit the example project, the resulting editor will update whenever the example file changes.)

[comment]: <> (We can also run the command by activating the `example1.hel` editor by choosing <span class="guilabel">Spoofax ‣ Debug ‣ Show parsed AST</span> from the main menu.)

[comment]: <> (Finally, we can run the command by right-clicking the `example1.hel` file in the <span class="guilabel">Package Explorer</span> by choosing <span class="guilabel">Spoofax ‣ Debug ‣ Show parsed AST</span> from the context menu.)
