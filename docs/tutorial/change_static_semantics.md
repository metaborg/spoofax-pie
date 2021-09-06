## Changing the static semantics

Now we will change the static semantics of the language.
Open the main Statix file `helloworld/src/main.stx`
[Statix](https://www.spoofax.dev/references/statix/) is a meta-language for defining the static semantics of your language, which includes type checking.

First we will update the Statix specification to handle the new language constructs.
Replace the `#!statix programOk(_).` line with `programOk(Program(parts)) :- partsOk(parts).`, meaning that we accept programs consisting of parts, as long as their parts are ok.
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

??? note "`src/main.stx` full contents"
    ```statix
    module main

    imports

      signatures/start-sig

    rules

      programOk : Start
      programOk(Program(parts)) :- partsOk(parts).

      partOk : Part
      partOk(Hello()).
      partOk(World()) :- try { false } | warning $[World!].
      partsOk maps partOk(list(*))
    ```

Build the project, and a warning marker should appear under all instances of `world` in the example program.
