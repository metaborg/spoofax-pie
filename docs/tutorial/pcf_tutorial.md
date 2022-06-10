# A Spoofax tutorial implementing Programming Computable Functions (PCF)

> In computer science, Programming Computable Functions (PCF) is a typed functional language introduced by Gordon Plotkin in 1977, based on previous unpublished material by Dana Scott. It can be considered to be an extended version of the typed lambda calculus or a simplified version of modern typed functional languages such as ML or Haskell.

-- https://en.wikipedia.org/wiki/Programming_Computable_Functions

With this tutorial you will hopefully be able to define the programming language PCF in the [Spoofax language workbench](https://www.spoofax.dev/spoofax-pie/develop/) in around one hour. In order to get started quickly, you should clone the [Git repository](https://github.com/MetaBorgCube/pcf-tutorial/) connected to this tutorial. The `template` directory contains a minimal project setup that allows you to start implementing PCF. Based on this tutorial, you are hopefully able to implement the syntax and static semantics of PCF yourself. In case you get stuck, you can always peek at the example implementation in the `implementation` directory.

## Getting started

You will need an installation of the Spoofax language workbench, you can find [the latest release here](https://www.spoofax.dev/spoofax-pie/develop/tutorial/install/). The recommended version to download is the one with embedded JVM. Note that on MacOS and Linux there are some extra instructions after unpacking.

You can now import the project following the [instructions on the website](https://www.spoofax.dev/spoofax-pie/develop/guide/eclipse_lwb/import/), which come down to `File > Import...`; `General > Existing Projects into Workspace`; `Next >`; tick `Select root directory:`; `Browse...` to the directory with the `spoofaxc.cfg` and press `Open`; `Finish`.

Now you should `Project > Build Project` after selecting the project in the `Package Explorer`.

Within the project you will be working on files in the `src` and `test` directories.

## Source material for PCF

Since the original publications on PCF were more concerned with semantics than syntax, it is a bit difficult to trace the syntax of PCF. We will use the definition of PCF from a book by John C. Mitchell called "Foundation for Programming Languages" (Mitchell 1996). Note that you might also find a definition by Dowek and Levy (Dowek et al. 2011) called Mini-ML, or PCF, this is not the version that we will use here. Chapter 2 of Foundation for Programming Languages about the language PCF is [freely available](https://theory.stanford.edu/~jcm/books/fpl-chap2.ps).

## Grammar

This is a slightly massaged grammar of so-called "pure pcf" from section 2.2.6:

```
e ::= x                            (variable reference)
  | if e then e else e             (if condition)
  | \x : t. e                      (function abstraction)
  | e e                            (function application)
  | fix e                          (fixed point)
  | true | false                   (boolean constants)
  | Eq? e e                        (equality check)
  | n                              (natural number constant)
  | e + e                          (arithmetic operations)
  | (e)                            (parenthesised expression)

t ::= nat                          (natural number type)
  | bool                           (boolean type)
  | t -> t                         (function type)
  | (t)                            (parenthesised type)
```

As you can see, PCF is a small functional programming language. It is an expression based language where `e` is the expression sort, and `t` is the type sort. There are also lexical sorts in this grammar, namely `x` for names, and `n` for numeric constants. The other words and symbols are keywords and operators. We've replaced some of the non-ASCII notation from the book into an ASCII version to make it easier to type, but if you have a good input method for non-ascii symbols, feel free to use those in your grammar. We've also added parentheses to both sorts for grouping. We have excluded pairs from the grammar, to make the language a little smaller and hopefully make this tutorial completable in one hour.

The grammar in the book is more type-directed, which constrains what programs the parser can parse to already be closer to the set of programs that are actually typed and therefore in the language. While this might seem advantageous, in practice it is nicer for a user to have a wide range of programs parse and be given highlighting. The type checker can give a much clearer explanation of why a program is not acceptable than a parser based on a grammar that encodes some type information.

### SDF3

With a context-free grammar available to us, we can start implementing the syntax of PCF in Spoofax. For this we use SDF3, the Syntax Definition Formalism version 3. You'll find that the `template` project already has some `.sdf3` files ready for you: `lex.sdf3`, `expr.sdf`, `type.sdf3` and `main.sdf3`.

The main file defines the `start symbol` of the grammar which is used by the editor to know where to start parsing. Expressions go in `expr.sdf3`, types in `type.sdf3` and we have already provided you with a lexical syntax in `lex.sdf3`. Have a look, you will find the definition of lexical sorts `Name`, `Number` and `Keyword`, where `Name` and `Number` are defined as regular expressions, and `Keyword` is defined as a few options of literal strings that correspond with keywords from the grammar. Then `Name` is restricted by a rejection rule to not match anything that can be parsed as `Keyword`. The `lexical restrictions` make sure that names and numbers are matched greedily.

Aside: Grammars in SDF3 define both lexical and context-free syntax, both of which are handled together by a character-level parsing algorithm called SGLR. This makes it hard to provide truly greedy regular expression by default, and instead we express that a name is not allowed to be directly followed by a letter or number, which can be handled better by a parser and still makes things greedy.

At the end of the file you find a defining of `LAYOUT`, which is the white space (and possibly comments) that are allowed between parts of the context-free syntax that we'll specify together for expressions and types.

In the files `expr.sdf3` and `type.sdf3` you will find the sort definitions for expressions and types with some but not all rules. As you can see, there is [template syntax](https://www.metaborg.org/en/latest/source/langdev/meta/lang/sdf3/reference.html#templates) in SDF3 which is both a convenient way to write your syntax and a hint for how it might be formatted in a program.

> Exercise. Try writing the remaining part of the grammar yourself.

Once you've built the project again, you can try out the newly added parts of a grammar. In `test/test.spt` file you will find test written in the [SPoofax Testing language SPT](https://www.metaborg.org/en/latest/source/langdev/meta/lang/spt/index.html). In this special language workbench testing language we can test many things on a high level. For now we can more `parse succeeds` and `parse fails` tests and see failing tests get an error marker in the editor immediately. You can also with a `parse to` test where you can specify the abstract syntax tree you expect.

Of course you can also write your PCF programming language in its own editor. There is already an `example.pcf` file, where you can see the syntax highlighting derived from your grammar. You can also view the abstract syntax tree of this program through the menu `Spoofax > Debug > Show parsed AST`. The `(continuous)` version even updates as you update your program.

You might find that writing a program `1 + 1 + 1` fails both expectations, because it is in our PCF language, but the parsing isn't entirely _successful_. Instead the result, which you can also write as an expectation, is `parse ambiguous`. We need to specify in the grammar what the associativity of the program is, whether it's `(1 + 1) + 1` or `1 + (1 + 1)`. Let's pick the former and use the `{left}` annotation on the `Expr.Add` rule. Now your double-add test should work. In fact we can now use the `parse to` test to specify that we expect `Add(Add(Num("1"), Num("1")), Num("1"))`. That is a little cumbersome to write though. What we can also do is write another program between double brackets: `[[(1 + 1) + 1]]`. Because the round brackets are not in the AST, this comes down to the same test.

Now that we're familiar with ambiguities and testing for them, we should root out the other ones in our grammar. You'll find that most grammar productions in PCF are not ambiguous with themselves, but mostly with each other. This is a priority problem, which is specified in a `context-free priorities` section of the grammar. You can write `Expr.App > Expr.Add` to specify that application binds tighter than addition. You can write out pairs of these with commas in between, or a longer chain of `>`, which is more common and is a reminder that priority is transitive. You can also make groups of expressions of the same priority, like `{ Expr.If  Expr.Lam  Expr.Fix }`.

> Exercise. See if you can figure out a good set of priorities and write some tests for them. You can check your list against ours in the `implementation`.

## Static Semantics

PCF has a relatively simple type system, defined as follows:

```
---------------- [T-True]
Γ |- true : bool

----------------- [T-False]
Γ |- false : bool

------------ [T-Nat]
Γ |- n : nat


Γ |- e1 : nat
Γ |- e2 : nat
------------------ [T-Add]
Γ |- e1 + e2 : nat

Γ |- e1 : t
Γ |- e2 : t
--------------------- [T-Eq]
Γ |- Eq? e1 e2 : bool

Γ |- e : bool
Γ |- e1 : t
Γ |- e2 : t
----------------------------- [T-If]
Γ |- if e then e1 else e2 : t


(x, t) ϵ Γ
---------- [T-Var]
Γ |- x : t

(x, t); Γ |- e : t'
------------------------ [T-Abs]
Γ |- \x : t. e : t -> t'

Γ |- e1 : t' -> t
Γ |- e2 : t'
----------------- [T-App]
Γ |- e1 e2 : t


Γ |- e : t -> t
--------------- [T-Fix]
Γ |- fix e : t
```

In this type system, all constructs have monomorphic types. The equality operator accepts any operands, as long as the types of the left and right operand are equal. Similarly, the types of the then-branch and the else-branch of an if-expression should be equal. Variables are typed in a typing environment Γ, which is extended by lambda abstractions. This is all similar to the regular definition of the lambda calculus (see e.g. Pierce (2002), chap. 9). The typing rule for the fixpoint operator ensures its argument is an endofunction, and types it with the (co)domain of the function.

### Statix

Given this type system and a parser derived from the syntax specification, we can start defining our type-checker in Spoofax. We use the Statix meta-language for type system specification for this. In Statix, type-systems can be expressed in a declarative style, closely related to formal inference rules, such as given above. However, instead of typing environments, _scope graphs_ are used for name binding. (Scope Graphs will be explained in more detail later in this tutorial.) The backend (often referred to as 'solver') interprets the specification applied to an AST of the object language (PCF in this case) as a constraint program, which yields an executable type checker.

So let's get started. We will define our type-system in the `src/expr.stx` file. This file already imports `expr-sig` and `type-sig`, which makes the abstract syntax of the language, which is derived from the syntax definition, available. In addition, there is a declaration for a user-defined constraint `typeOfExpr`, which has type `scope * Expr -> Type`. That means that the constraint accepts a `scope` and an expression, and returns a type for it. This can be read as `Γ |- e : t`, where the `scope` argument takes the role of the environment Γ. To test the type system, it is recommended to open the `example.pcf` and `test/test.spt` files as well.

A lonely constraint declaration is useless: there are no ways a `typeOfExpr` constraint can be solved. We need to add _rules_ for this constraint. Rules can be compared to _cases_ in a functional language or, even better, inference rules as given above. Lets look at an example:

```
typeOfExpr(_, True()) = Bool().
```

This rule states the simple fact that a `true` constant has type `bool`. Do you see the similarity with our `T-True` rule?

> Exercise: Define the `T-False` and `T-Nat` rules in your Statix specification.

That was not too hard. However, not all rules are that simple. Some of them have _premises_. In Statix, we encode them after a turnstile symbol `:-`. For example:

```
typeOfExpr(s, Eq(e1, e2)) = Bool() :- {T}
  typeOfExpr(s, e1) == T,
  typeOfExpr(s, e2) == T.
```

This rule is a bit more complicated, so lets break it down part by part. The meaning of the _rule head_ (`typeOfExpr(s, Eq(e1, e2)) = Bool()`) should be familiar by now. An equality comparison in scope `s` has type `Bool()`. Then, after the turnstile, there is an _existential_ constraint `{T}`, which introduced the unification variable `T`. One might read it as `∃ T. ...`. Then there are two _premises_ of the rule that assert that `e1` and `e2` have the type `T`. As a unification variable can only have a single value, this effectively enforces both operands to have the same type.

> Exercise: Define the `T-Add` and `T-If` rules.

It is also possible to use SPT to test your type system. To apply the type-checker in a test, use the `analysis succeeds` and `analysis fails` expectations. For example:

```
test cannot compare nat and bool [[
  Eq? true 42
]] analysis fails
```

> Exercise: Define some SPT tests that cover all currently typed constructs.

#### Scope Graphs

Now it is time to look add lambda abstractions and applications. In Statix, typing these constructs uses scope graphs. We will explore how scope graphs work using some example programs.

The rule for lambda abstraction is already defined as follows:

```
typeOfExpr(s, Lam(x, T, e)) = Fun(T, T') :- {s_lam}
  new s_lam,
  s_lam -P-> s,
  !var[x, T] in s_lam,
  typeOfExpr(s_lam, e) == T'.
```

In this rule, a lambda expression gets the type `Fun(T, T')`. The input type `T` is explicitly provided by the syntax, while the result type `T'` is inferred from the lambda body. However, as can be seen in the `T-Abs` rule, the body is typed in an _extended context_. In Statix, that is encoded by the first three constraints. The `new s_lam` constraint generates a fresh node in the scope graph, and binds a reference to that node to the unification variable `s_lam`. That scope encodes the context of the body. To indicate that `s_lam` inherits all declarations from its parent context (Γ in the rule), the `s_lam -P-> s` constraints asserts an _edge_ from `s_lam` to `s`. This ensures that a query in `s_lam` (explained later) can reach declarations in `s`. The edge is labeled with an edge label `P`. In Statix all edges are labeled, and labels have to be introduced explicitly. In the stub, the label `P` was already predefined in the `signature` section above the rule for abstractions.
Finally, we need to _extend_ the `s_lam` context with our new variable. This is what the `!var[x, T] in s_lam` constraint does. This constraint, which is similar to the `(x, t);` in `T-Abs`, creates a declaration with name `x` and type `T` in `s_lam`. A declaration always uses a _relation_, which is `var` in this case. The `var` relation is also declared in the `signature` section above.

To aid debugging, scope graph can be inspected. For example, open an `example.pcf` file, and add this program:

```
(\double: nat -> nat. double 21) \x: nat. x + x
```

Now, open the menu `Spoofax > Debug > Show formatted scope graph (continuous)`. A new window should be opened with (approximately) this content:

```
scope graph
  #-s_lam_20-4 {
    relations {
      expr!var : ("double", Fun(Nat(), Nat()))
    }
    edges {
      expr!P : #-s_glob_1-5
    }
  }
  #-s_lam_10-2 {
    relations {
      expr!var : ("x", Nat())
    }
    edges {
      expr!P : #-s_glob_1-5
    }
  }
```

This file shows two scopes: `#-s_lam_20-4` and `#-s_lam_10-2`. These scopes correspond to the bodies of the first and second abstraction, respectively. Both scopes have a single declaration, shown in the `relations` block. The contents of the relations should not be surprising: they are the declarations we asserted using our `!var[x, T] in s_lam` constraint! Similarly, there are two `P`-labeled edges to `#s_glob_1-5`. This scope is the global scope, which does not have its own entry because it is empty. In fact, it corresponds to the empty top-level environment a regular typing derivation would start with.

Now, we have seen how to extend a context, but how should we read it? Reading a context corresponds to doing a _query_ in Statix. In PCF, the only time we use a query is when resolving a variable in the `T-Var` rule. In Statix, this rule is defined as follows:

```
typeOfExpr(s, Var(x)) = T :- {Ts}
  query var
    filter P* and { x' :- x' == x }
       min $ < P
        in s |-> Ts,
  referenceTypeOk(x, T, Ts).
```

We ignore the `referenceTypeOk` constraint for now, as it is only there to ensure the error messages are easier to interpret. The interesting part is the `query` constraint. This constraint takes quite some parameters, which we will analyse one by one.

First, there is the `var` parameter, which is the relation to query. In languages that, unlike PCF, have multiple relations, this argument ensures that the query will only find declarations under the `var` relation, such as the ones asserted by the `T-Abs` rule we've discussed earlier.
Second, there is the `P*` argument. This is a regular expression that describes valid paths in the scope graph. In this case, it ensures that the query can resolve in the local context, and all parent contexts.

> Exercise. Change `P*` into `P+` and test the program `\x: nat. x`. Does it type correctly? Why (not)?

Third, there is the _data well-formedness_ condition `{ x' :- x' == x }`. This is an anonymous unary predicate that compares the name of a declaration (bound to `x'`) to the reference (`x`). Only declarations for which the constraint holds (i.e., `x' == x` can be solved) are returned in the query answer. This excludes reachable declarations with the wrong name.

> Exercise. Change `{ x' :- x' == x }` to `{ x' :- true }` and test the program `\x: nat. \y: bool. x + 1`. Does it type correctly? Why (not)?

Fourth, there is the `$ < P` argument. This argument indicates that the end-of-path label `$` _binds closer_ than the `P` label. This means that shorter paths are preferred over longer paths, essentially modelling shadowing. This is best seen in action:

> Exercise. Remove `$ < P` and test the program `\x: bool. \x: nat. x + 1`. Does it type correctly? Why (not)?

> Exercise. Add `P < $` and test the program `\x: bool. \x: nat. x + 1`. Does it type correctly? Why (not)?

> Exercise: Define the `T-App` and `T-Fix` rules.

Congratulations! You have now defined a fully functional frontend for PCF.

### A Small Detour on Editor Services.

Type information is often used for editor services and transformations. This is tightly integrated in the Spoofax language as well. For example, hover your mouse over a variable reference for a moment. After some time, a tooltip with (e.g.) the text `Type: Nat()` will show up. Or, even fancier, CTRL-Click (Cmd-Click on Mac) on a reference. Your cursor now should jump to the binder that introduced the variable.

To understand this behaviour, we have to look into the second `referenceTypeOk` rule. This rule contains the following constraints:

```
@x.type := T,
@x.ref := x'
```

In this rule `x` is the reference, and `T` is the expected type. The constraint `@x.type := T` sets the value of the `type` _property_ of `x` to `T`. The Spoofax editor will read such `type` properties and display them in a tooltip. That explains the tooltip we observed earlier.

In addition, the `x'` variable is the name of the declaration. Unobservable to the user, this name has its position attached. By assigning it as the `ref` property of the reference `x`, the editor reference resolution knew where to put the cursor when `x` was CTRL-clicked!


## Syntactic Sugar

These are the syntactic extensions (syntactic sugar) found in the book we're following:

```
e ::= ...
  | let x : t = e in e             (let binding)
  | let x(x : t) : t = e in e      (let function binding)
  | letrec x : t = e in e          (let recursive binding)
  | letrec x(x : t) : t = e in e   (let recursive binding)
```

These extensions are defined as sugar, in that you can transform them into an equivalent program using only the "pure pcf" syntax. That's exactly how we will also implement this sugar.

Let's first summarise the meaning of these extensions:

```
let x : t = e1 in e2
  == (\x : t. e2) e1

let x1(x2 : t1) : t2 = e1 in e2
  == let x1 : t1 -> t2 = \x2 : t1. e1 in e2
  == (\x1 : t1 -> t2. e2) (\x2 : t1. e1)

letrec x : t = e1 in e2
  == let x : t = fix \x : t. e1 in e2
  == (x : t. e2) (fix \x : t. e1)

letrec x1(x2 : t1) : t2 = e1 in e2
  == letrec x1 : t1 -> t2 = \x2 : t1. e1 in e2
  == let x1 : t1 -> t2 = fix \x1 : t1 -> t2. \x2 : t1. e1 in e2
  == (\x1 : t1 -> t2. e2) (fix \x1 : t1 -> t2. \x2 : t1. e1)
```

As you can see, we can use lambdas for binding variables in a `let`, and we can use `fix` for recursive definitions in `letrec`. Function definition syntax in `let` is of course also just sugar for lambdas.

### Stratego 2

Let's turn these equations into executable code. We will use the term-writing language Stratego 2 for this task. You can find code of this language in `.str2` files such as `main.str2` and `desugar.str2`. In the latter you will find a prewritten strategy `desugar`, defined as a type preserving transformation (`TP`) that traverses topdown and tries to apply the `desugar-let` rewrite rules. In the rewrite rules is `desugar-let`, also type preserving, but it currently fails. You can see an example rewrite rule that turns let bound functions into the applications of lambdas from our equations above, but this uses the `LetF` abstract syntax, which we have not defined yet. So before we start writing our rewrite rules, we should first define the appropriate syntax in `expr.sdf3`.

> Exercise: Define syntax for `let` and `letrec`, and don't forget the priority rules of the newly added syntax.

With the syntax defined, we can now start writing our rewrite rules. As suggested by the example one for let-bound functions, we are writing the abstract syntax pattern of our sugar programs with variables, then an arrow `->`, and then the abstract syntax pattern of our final result. This way we only have to traverse our program once to apply the rules. We could go `topdown` or `bottomup` with this approach, either direction works.

> Exercise: Write the remaining rewrite rules.

We _could_ also write our desugaring differently, in the smaller steps of the equation, by repeatedly applying rewrite rules. Our strategy would then be `outermost` or `innermost`, and we could rewrite our `Let` as usual, but write our `Let(Rec)F` to `Let(Rec)` and our `LetRec` to `Let` with `Fix`.

> Exercise: Write the single step rewrite rules.
> (This is demonstrated in `desugar2` of the example `implementation`.)

You can factor out the similarity of the function-binding `let` and `letrec` to their normal counterpart.

> Bonus Exercise: Define new constructors for `Let` and `LetF` that take an extra argument that marks if it is a normal or `letrec` version. Define a separate desugaring step to transform your program into these constructors. Now define the single step rewrite rules from before, combining the similar one into a single rule.
> (This is demonstrated in `desugar3` of the example `implementation`.)

With your desugaring now complete, you can add a call to `desugar` to `pre-analyze` in `src/main.str2`. This will eliminate the `let` related constructors before you analyse your program, so you do not need to extend your Statix specification.

## References

- (Mitchell 1996) Mitchell, John C. (1996). The Language PCF. In: Foundations for Programming Languages. https://theory.stanford.edu/~jcm/books/fpl-chap2.ps
- (Dowek et al. 2011) Dowek, G., Lévy, JJ. (2011). The Language PCF. In: Introduction to the Theory of Programming Languages. Undergraduate Topics in Computer Science. Springer, London. https://doi.org/10.1007/978-0-85729-076-2_2
- (Pierce 2002) Pierce, Benjamin C. (2002). Types and Programming Languages. MIT Press, Cambridge, Massachusetts.
