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
[SDF3](https://www.spoofax.dev/references/syntax/) is a meta-language (i.e., a language to describe languages) for describing the syntax of a language, from which Spoofax will derive the parser of your language.
Under the `#!sdf3 context-free syntax` section, replace the `#!sdf3 Start.Empty = <>` line with `#!sdf3 Start.Program = <<{Part " "}*>>`, indicating that the language accepts programs which consists of zero or more parts.

`#!sdf3 Part` is a *sort* and must be defined by adding its name to the `#!sdf3 context-free sorts` section on a new line.

Now we will add syntax productions to `#!sdf3 Part` to the `#!sdf3 context-free syntax` section.
Add `#!sdf3 Part.Hello = <hello>` on a new line, indicating that one sort of `#!sdf3 Part` is the word `hello.`
Then add `#!sdf3 Part.World = <world>` on a new line, indicating that one sort of `#!sdf3 Part` is the word `world`.

??? note "`src/start.sdf3` full contents"
    ```sdf3
    module start

    context-free start-symbols

      Start

    context-free sorts

      Start
      Part

    context-free syntax

      Start.Program = <<{Part " "}*>>
      Part.Hello = <hello>
      Part.World = <world>

    lexical syntax

      LAYOUT = [\ \n\v\f\r]

    context-free restrictions

      LAYOUT? -/- [\ \n\v\f\r]
    ```

To observe our changes, build the project by clicking on the project in the <span class="guilabel">Package Explorer</span> and choosing <span class="guilabel">Project ‣ Build Project</span> from the main menu, or by pressing ++cmd+b++ on macOS or ++ctrl+b++ on others.
To see when the build is done, open the progress window by choosing <span class="guilabel">Window ‣ Show View ‣ Progress</span>.
If the progress view is empty, the build is done.
The initial build can be a bit slow because there is a lot of code to compile in the background.
Subsequent builds will be faster due to incrementalization.

Create an example file for your language by right-clicking the project and choosing <span class="guilabel">New ‣ File</span>, filling in `example/example1.hel` as file name, and pressing <span class="guilabel">Finish</span>.
Type a valid sentence such as `hello world hello hello world` in this file, and it will highlight purple indicating that `hello` and `world` are keywords.

We can also run a debugging command on the example file to check the AST that the parser produces.
There are three ways to run this debugging command:

* Make sure the `example1.hel` editor is open and active (i.e., has focus), and choose from the main menu: <span class="guilabel">Spoofax ‣ Debug ‣ Show parsed AST</span>.
* Make sure the `example1.hel` editor is open and active (i.e., has focus), right-click in the editor and choose: <span class="guilabel">HelloWorld ‣ Debug ‣ Show parsed AST</span>.
* Right-click the `example/example1.hel` file in the <span class="guilabel">Package Explorer</span> and choose: <span class="guilabel">HelloWorld ‣ Debug ‣ Show parsed AST</span>

Running this command will open a new editor with the AST of the program, such as:

```aterm
Program([Hello(), World()])
```

There are also `(continuous)` variants of these debugging commands which will keep updating the debugging view when the source program changes.
You can drag tabs to the sides of the screen to keep multiple editors open simultaneously, for example to keep the continuous debugging view visible, and to keep the syntax definition files editable.

!!! warning
    There is currently a bug where continuous debugging views are not updated any more after the language is rebuilt. In that case, you have to open the continuous debugging view again.

## Testing the new syntax

We can also systematically test the syntax (and other facets) of the language with the [Spoofax Testing Language (SPT)](https://www.spoofax.dev/references/testing/).
Open the SPT file `helloworld/test/test.spt`.
This file contains one test which tests that the empty program parses successfully, which is still the case because a program can consist of 0 parts.

Add a new test case to the test suite by adding:

```spt
test hello world parse [[
  hello world
]] parse succeeds
```

which tests that `hello world` parses successfully.

You can also add negative tests such as:

```spt
test gibberish [[
  asdfasdfasdf
]] parse fails
```

??? note "`test/test.spt` full contents"
    ```spt
    module test

    test parse empty [[]] parse succeeds

    test hello world parse [[
      hello world
    ]] parse succeeds

    test gibberish [[
      asdfasdfasdf
    ]] parse fails
    ```

If you keep the SPT file open and rebuild your language, the SPT tests will be re-executed to provide feedback whether your change to the language conforms to your tests.
You can also run all SPT tests by right-clicking a directory with SPT files, or by right-clicking the language project, and choosing <span class="guilabel">SPT ‣ Run SPT tests</span>.
This will (once the tests are done executing) pop up a <span class="guilabel">SPT Test Runner</span> view with the results of testing.

## Changing syntax highlighting

Now we will change the syntax highlighter of the language.
Open the main ESV file `helloworld/src/main.esv`.
ESV is a meta-language for describing the syntax highlighter.
Change the `#!esv keyword : 127 0 85 bold` line to `#!esv keyword: 0 0 150 bold` and build the project.
Then check your `example1.hel` example file, it should now be highlighted blue.

To make iteration easier, you can drag the `example1.hel` tab to the side of the screen to open the language definition and example file side-by-side.
You can play around with the coloring a bit and choose a style to your liking.
Remember to rebuild the project after making a change to the language definition.

## Troubleshooting

Check the [troubleshooting guide](../guide/eclipse_lwb/troubleshooting.md) if you run into problems.
