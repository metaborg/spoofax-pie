---
title: "Semantic Code Completion"
---
# How to Enable Semantic Code Completion
Semantic code completion is now part of Spoofax 3.

## Enabling Semantic Code Completion
To enable support for semantic code completion in your language:

1.  Add the following to your language's `spoofaxc.cfg` file:

    ```cfg
    tego-runtime {}
    code-completion {}
    ```

2.  Declare the following strategies in your language's `main.str2` file, where `MyLang` is the name of your languages (as defined in `spoofaxc.cfg`:

    ```stratego
    rules // Analysis

      downgrade-placeholders = downgrade-placeholders-MyLang
      upgrade-placeholders   = upgrade-placeholders-MyLang
      is-inj                 = is-MyLang-inj-cons
      pp-partial             = pp-partial-MyLang-string
      pre-analyze            = explicate-injections-MyLang
      post-analyze           = implicate-injections-MyLang
    ```

3.  In your Statix files, for each rule define a predicate that accepts a placeholder where a syntactic sort is permitted. For example:

    ```statix
    rules // placeholders
    
      programOk(Module-Plhdr()).

      declOk(_, Decl-Plhdr()).

      typeOfExp(_, Exp-Plhdr()) = _.
    ```


## Using Semantic Code Completion
In Eclipse, in a file of your language, type the placeholder somewhere where it is permitted. Unless overridden, the placeholder is a sort name within double square brackets, such as `[[Exp]]`. Then put the caret on the placeholder and press ++command+space++ on macOS (or ++ctrl+space++ on Linux/Windows) to invoke code completion.

!!! tip "In a future release the placeholder will not need to be input explicitly."

## Limitations
For this first release of semantic code completion, there are some limitations:

- You have to type the placeholder explicitly to invoke code completion.
- If the file contains errors, code completion might fail to return results.
- If you have _catch-all_ predicates, code completion will not work. For example:
  ```statix
  // This prevents code completion from finding completions:
  typeOfExp(_, _) = _ :-
    try { false } | warning $[This expression is not yet implemented].
  ```

These limitations will be lifted in subsequent releases.
