package mb.spoofax.runtime.benchmark.state;

import com.google.common.collect.Lists;
import mb.spoofax.runtime.benchmark.ChangeMaker;
import mb.spoofax.runtime.benchmark.state.exec.BUTopsortState;
import mb.spoofax.runtime.benchmark.state.exec.TDState;
import mb.vfs.path.PPath;
import mb.vfs.path.PPaths;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.util.HashSet;


@State(Scope.Benchmark)
public class ChangesState {
    private PPath root;


    public void setup(WorkspaceState workspaceState) {
        this.root = workspaceState.root;
    }

    public void reset() {
        run(root, "git", "reset", "--hard");
        run(root, "git", "clean", "-ddffxx");
    }


    public void exec(TDState state, Blackhole blackhole) {
        final ChangeMaker changeMaker = changesKind.createChangeMaker(root, blackhole);
        changeMaker.run(state);
    }

    public void exec(BUTopsortState state, Blackhole blackhole) {
        final ChangeMaker changeMaker = changesKind.createChangeMaker(root, blackhole);
        changeMaker.run(state);
    }


    @Param({"medium"}) public ChangesKind changesKind;

    public enum ChangesKind {
        debug_syntax_cascading {
            @Override public ChangeMaker createChangeMaker(PPath root, Blackhole blackhole) {
                return new ChangeMaker() {
                    @Override protected void apply() {
                        // Initial execution.
                        final PPath calcProject = root.resolve("lang.calc");
                        addProject(calcProject, blackhole, "initial lang.calc");
                        execInitial(blackhole, "initial");

                        // Syntax specification: cascading change.
                        {
                            // Open editor.
                            final PPath syntaxFile = calcProject.resolve("syntax/Calc.sdf3");
                            String syntaxText = read(syntaxFile);
                            //execEditor(syntaxText, syntaxFile, calcProject, blackhole, "editor open cascading syntax/Calc.sdf3");
                            // Change editor text.
                            syntaxText = syntaxText.replace("Exp.Num = NUM", "Exp.Num = ID");
                            //execEditor(syntaxText, syntaxFile, calcProject, blackhole, "editor change cascading syntax/Calc.sdf3");
                            // Save file.
                            write(syntaxFile, syntaxText);
                            execPathChanges(syntaxFile, blackhole, "file change cascading syntax/Calc.sdf3");
                        }
                    }
                };
            }
        },
        debug_all_change {
            @Override public ChangeMaker createChangeMaker(PPath root, Blackhole blackhole) {
                return new ChangeMaker() {
                    @Override protected void apply() {
                        // Initial execution.
                        final PPath calcProject = root.resolve("lang.calc");
                        addProject(calcProject, blackhole, "initial lang.calc");
                        final PPath tigerProject = root.resolve("lang.tiger");
                        addProject(tigerProject, blackhole, "initial lang.tiger");
                        final PPath mjProject = root.resolve("lang.minijava");
                        addProject(mjProject, blackhole, "initial lang.minijava");
                        execInitial(blackhole, "initial");

                        // Extrema change: everything
                        try {
                            final HashSet<PPath> changedPaths = new HashSet<>();
                            root.walk(PPaths.extensionsPathWalker(Lists.newArrayList(
                                "cfg", "esv", "sdf3", "nabl2", "str", "calc", "mj", "tig"
                            ))).forEach(path -> {
                                String text = read(path);
                                text = text + " ";
                                write(path, text);
                                changedPaths.add(path);
                            });
                            execPathChanges(changedPaths, blackhole, "all files changed");
                        } catch(IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
            }
        },
        medium {
            @Override public ChangeMaker createChangeMaker(PPath root, Blackhole blackhole) {
                return new ChangeMaker() {
                    @Override protected void apply() {
                        // Initial execution.
                        final PPath calcProject = root.resolve("lang.calc");
                        addProject(calcProject, blackhole, "initial lang.calc");
                        final PPath tigerProject = root.resolve("lang.tiger");
                        addProject(tigerProject, blackhole, "initial lang.tiger");
                        final PPath mjProject = root.resolve("lang.minijava");
                        addProject(mjProject, blackhole, "initial lang.minijava");
                        execInitial(blackhole, "initial");

                        // Example program.
                        {
                            // Open editor.
                            final PPath programFile = calcProject.resolve("example/basic/gt.calc");
                            String programText = read(programFile);
                            execEditor(programText, programFile, calcProject, blackhole, "editor open example/basic/gt.calc");
                            // Change editor text.
                            programText = "4 > 5";
                            execEditor(programText, programFile, calcProject, blackhole, "editor change 1 example/basic/gt.calc");
                            programText = "4 > ";
                            execEditor(programText, programFile, calcProject, blackhole, "editor change 2 example/basic/gt.calc");
                            programText = "4 > 2;";
                            execEditor(programText, programFile, calcProject, blackhole, "editor change 3 example/basic/gt.calc");
                            // Save file.
                            write(programFile, programText);
                            execPathChanges(programFile, blackhole, "file change example/basic/gt.calc");
                        }

                        // Add new example program.
                        {
                            // Open editor.
                            final PPath programFile = calcProject.resolve("example/basic/lt.calc");
                            String programText = "";
                            execEditor(programText, programFile, calcProject, blackhole, "editor open new example/basic/lt.calc");
                            // Change editor text.
                            programText = "0 < 1;";
                            execEditor(programText, programFile, calcProject, blackhole, "editor change new example/basic/lt.calc");
                            // Save file.
                            write(programFile, programText);
                            execPathChanges(programFile, blackhole, "file create example/basic/lt.calc");
                        }

                        // Edit multiple example programs.
                        {
                            final PPath programFile1 = tigerProject.resolve("example/appel/test01.tig");
                            String programText1 = read(programFile1);
                            execEditor(programText1, programFile1, tigerProject, blackhole, "editor open example/appel/test01.tig");
                            final PPath programFile2 = tigerProject.resolve("example/appel/test02.tig");
                            String programText2 = read(programFile2);
                            execEditor(programText2, programFile2, tigerProject, blackhole, "editor open example/appel/test02.tig");
                            final PPath programFile3 = tigerProject.resolve("example/appel/test03.tig");
                            String programText3 = read(programFile3);
                            execEditor(programText3, programFile3, tigerProject, blackhole, "editor open example/appel/test03.tig");

                            programText1 = programText1.replace("arrtype [10]", "arrtype [20]");
                            execEditor(programText1, programFile1, tigerProject, blackhole, "editor change example/appel/test01.tig");
                            programText2 = programText2.replace("arrtype [10]", "arrtype [20]");
                            execEditor(programText2, programFile2, tigerProject, blackhole, "editor change example/appel/test02.tig");
                            programText3 = programText3.replace("Somebody", "Everyone");
                            execEditor(programText3, programFile3, tigerProject, blackhole, "editor change example/appel/test03.tig");

                            write(programFile1, programText1);
                            write(programFile2, programText2);
                            write(programFile3, programText3);
                            execPathChanges(blackhole, "file change example/appel/{test01.tig,test02.tig,test03.tig}", programFile1,
                                programFile2, programFile3);
                        }

                        // Syntax styling specification.
                        {
                            // Open editor.
                            final PPath stylingFile = calcProject.resolve("style/style.esv");
                            String stylingText = read(stylingFile);
                            execEditor(stylingText, stylingFile, calcProject, blackhole, "editor open style/style.esv");
                            // Change editor text.
                            stylingText = stylingText.replace("keyword    : 127 0 85 bold", "keyword    : ");
                            execEditor(stylingText, stylingFile, calcProject, blackhole, "editor change 1 style/style.esv");
                            stylingText = stylingText.replace("keyword    : ", "keyword    : 255 0 0");
                            execEditor(stylingText, stylingFile, calcProject, blackhole, "editor change 2 style/style.esv");
                            stylingText = stylingText.replace("keyword    : 255 0 0", "keyword    : 255 0 0 0 255 0");
                            execEditor(stylingText, stylingFile, calcProject, blackhole, "editor change 3 style/style.esv");
                            // Save file.
                            write(stylingFile, stylingText);
                            execPathChanges(stylingFile, blackhole, "file change style/style.esv");
                        }

                        // Add styling specification.
                        {
                            // Open syntax styling editor.
                            final PPath stylingFile = tigerProject.resolve("style/style.esv");
                            String stylingText = "";
                            execEditor(stylingText, stylingFile, tigerProject, blackhole, "editor open new style/style.esv");
                            // Change syntax styling text.
                            stylingText = "module style\n" +
                                "\n" +
                                "colorer\n" +
                                "\n" +
                                "  keyword    : 127 0 85 bold\n" +
                                "  identifier : 0 0 150 bold\n" +
                                "  string     : 0 0 255\n" +
                                "  number     : 0 127 0\n" +
                                "  operator   : 0 0 128\n" +
                                "  layout     : 63 127 95 italic\n" +
                                "  default    : 0 0 0\n" +
                                "  unknown    : 0 0 0\n";
                            execEditor(stylingText, stylingFile, tigerProject, blackhole, "editor change new style/style.esv");
                            // Write new file.
                            write(stylingFile, stylingText);
                            execPathChanges(stylingFile, blackhole, "file create style/style.esv");
                        }

                        // Edit multiple styling specifications.
                        {
                            // Calc
                            final PPath calcStylingFile = calcProject.resolve("style/style.esv");
                            String calcStylingText = read(calcStylingFile);
                            execEditor(calcStylingText, calcStylingFile, calcProject, blackhole, "editor open lang.calc/style/style.esv");
                            calcStylingText = calcStylingText.replace("number     : 0 127 0", "number     : 0 127 127");
                            execEditor(calcStylingText, calcStylingFile, calcProject, blackhole, "editor change lang.calc/style/style.esv");
                            write(calcStylingFile, calcStylingText);
                            // Tiger
                            final PPath tigStylingFile = tigerProject.resolve("style/style.esv");
                            String tigStylingText = read(tigStylingFile);
                            execEditor(tigStylingText, tigStylingFile, tigerProject, blackhole, "editor open lang.tiger/style/style.esv");
                            tigStylingText = tigStylingText.replace("number     : 0 127 0", "number     : 0 127 127");
                            execEditor(tigStylingText, tigStylingFile, tigerProject, blackhole, "editor change lang.tiger/style/style.esv");
                            write(tigStylingFile, tigStylingText);
                            // Exec changes
                            execPathChanges(blackhole, "file change {lang.calc,lang.tiger}/style/style.esv", calcStylingFile,
                                tigStylingFile);
                        }

                        // Add minijava language.
                        {
                            final PPath langSpecFile = mjProject.resolve("langspec.cfg");
                            String langSpecText = "";
                            execEditor(langSpecText, langSpecFile, mjProject, blackhole, "editor open new lang.minijava/langspec.cfg");
                            langSpecText = "langspec {\n" +
                                "  identification {\n" +
                                "    file extensions: mj\n" +
                                "  }\n" +
                                "  information {\n" +
                                "    name: MiniJava\n" +
                                "  }\n" +
                                "  syntax {\n" +
                                "    parse {\n" +
                                "      files:\n" +
                                "        syntax/minijava.sdf3\n" +
                                "      , syntax/lex.sdf3\n" +
                                "      , syntax/classes.sdf3\n" +
                                "      , syntax/mainclass.sdf3\n" +
                                "      , syntax/statements.sdf3\n" +
                                "      , syntax/expressions.sdf3\n" +
                                "      main file: syntax/minijava.sdf3\n" +
                                "      start symbol id: Start\n" +
                                "    }\n" +
                                "    signature {\n" +
                                "      files:\n" +
                                "        syntax/minijava.sdf3\n" +
                                "      , syntax/lex.sdf3\n" +
                                "      , syntax/classes.sdf3\n" +
                                "      , syntax/mainclass.sdf3\n" +
                                "      , syntax/statements.sdf3\n" +
                                "      , syntax/expressions.sdf3\n" +
                                "    }\n" +
                                "    style {\n" +
                                "      file: style/style.esv\n" +
                                "    }\n" +
                                "  }\n" +
                                "  names and types {\n" +
                                "    nabl2 files:\n" +
                                "      nats/minijava.nabl2\n" +
                                "    , nats/classes.nabl2\n" +
                                "    , nats/statements.nabl2\n" +
                                "    , nats/expressions.nabl2\n" +
                                "    stratego config: {\n" +
                                "      main file: nats/nats.str\n" +
                                "      include dirs:\n" +
                                "        trans,\n" +
                                "        nats,\n" +
                                "        src-gen,\n" +
                                "        src-gen/nabl2,\n" +
                                "        /Users/gohla/spoofax/master/repo/spoofax-releng/spoofax/meta.lib.spoofax/trans,\n" +
                                "        /Users/gohla/spoofax/master/repo/spoofax-releng/nabl/org.metaborg.meta.nabl2.shared/trans,\n" +
                                "        /Users/gohla/spoofax/master/repo/spoofax-releng/nabl/org.metaborg.meta.nabl2.shared/src-gen,\n" +
                                "        /Users/gohla/spoofax/master/repo/spoofax-releng/nabl/org.metaborg.meta.nabl2.runtime/trans,\n" +
                                "        /Users/gohla/spoofax/master/repo/spoofax-releng/nabl/org.metaborg.meta.nabl2.runtime/src-gen,\n" +
                                "        /Users/gohla/spoofax/master/repo/spoofax-releng/nabl/org.metaborg.meta.nabl2.runtime/src-gen/nabl2,\n" +
                                "        /Users/gohla/spoofax/master/repo/spoofax-releng/nabl/org.metaborg.meta.nabl2.lang/trans,\n" +
                                "        /Users/gohla/spoofax/master/repo/spoofax-releng/nabl/org.metaborg.meta.nabl2.lang/src-gen\n" +
                                "      include libs: stratego-lib, stratego-sglr\n" +
                                "      base dir: .\n" +
                                "      cache dir: target/str-cache\n" +
                                "      output file: target/nats.ctree\n" +
                                "    }\n" +
                                "    stratego strategy id: nats\n" +
                                "    root scope per file: true\n" +
                                "  }\n" +
                                "}\n";
                            execEditor(langSpecText, langSpecFile, mjProject, blackhole, "editor change new lang.minijava/langspec.cfg");
                            write(langSpecFile, langSpecText);
                            execPathChanges(langSpecFile, blackhole, "file create lang.minijava/langspec.cfg");
                        }

                        // Syntax specification: small/local change.
                        {
                            // Open editor.
                            final PPath syntaxFile = calcProject.resolve("syntax/CalcLexical.sdf3");
                            String syntaxText = read(syntaxFile);
                            final String originalSyntaxText = syntaxText;
                            execEditor(syntaxText, syntaxFile, calcProject, blackhole, "editor open syntax/CalcLexical.sdf3");
                            // Change editor text.
                            syntaxText = syntaxText.replace("INT      = \"-\"? [0-9]+", "INT      = \"-\"? [8-9]+");
                            execEditor(syntaxText, syntaxFile, calcProject, blackhole, "editor change small syntax/CalcLexical.sdf3");
                            // Save file.
                            write(syntaxFile, syntaxText);
                            execPathChanges(syntaxFile, blackhole, "file change small syntax/CalcLexical.sdf3");
                            // Change back and save.
                            execEditor(originalSyntaxText, syntaxFile, calcProject, blackhole,
                                "editor change small undo syntax/CalcLexical.sdf3");
                            write(syntaxFile, originalSyntaxText);
                            execPathChanges(syntaxFile, blackhole, "file change small undo syntax/CalcLexical.sdf3");
                        }

                        // Syntax specification: cascading change.
                        {
                            // Open editor.
                            final PPath syntaxFile = calcProject.resolve("syntax/Calc.sdf3");
                            String syntaxText = read(syntaxFile);
                            final String originalSyntaxText = syntaxText;
                            execEditor(syntaxText, syntaxFile, calcProject, blackhole, "editor open cascading syntax/Calc.sdf3");
                            // Change editor text.
                            syntaxText = syntaxText.replace("Exp.Num = NUM", "Exp.Num = ID");
                            execEditor(syntaxText, syntaxFile, calcProject, blackhole, "editor change cascading syntax/Calc.sdf3");
                            // Save file.
                            write(syntaxFile, syntaxText);
                            execPathChanges(syntaxFile, blackhole, "file change cascading syntax/Calc.sdf3");
                            // Change back and save.
                            execEditor(originalSyntaxText, syntaxFile, calcProject, blackhole,
                                "editor change cascading undo syntax/Calc.sdf3");
                            write(syntaxFile, originalSyntaxText);
                            execPathChanges(syntaxFile, blackhole, "file change cascading undo syntax/Calc.sdf3");
                        }

                        // Refactor syntax specification: move into new file.
                        {
                            final PPath syntaxFile = mjProject.resolve("syntax/minijava.sdf3");
                            final PPath mainclassSyntaxFile = mjProject.resolve("syntax/mainclass.sdf3");

                            // Remove main class rule from minijava.sdf3
                            String syntaxText = read(syntaxFile);
                            execEditor(syntaxText, syntaxFile, mjProject, blackhole, "editor open refactor syntax/minijava.sdf3");
                            syntaxText = syntaxText.replace(
                                "MainClass.MainClass = <class <ID> { public static void main (String[] <ID>) { <Statement> } }>",
                                ""
                            );
                            execEditor(syntaxText, syntaxFile, mjProject, blackhole, "editor change 1 refactor syntax/minijava.sdf3");
                            // Add import from minijava.sdf3 to mainclass.sdf3
                            syntaxText = syntaxText.replace("classes", "classes mainclass");
                            execEditor(syntaxText, syntaxFile, mjProject, blackhole, "editor change 2 refactor syntax/minijava.sdf3");
                            write(syntaxFile, syntaxText);

                            // Add to mainclass.sdf3
                            String mainclassSyntaxText = read(mainclassSyntaxFile);
                            execEditor(mainclassSyntaxText, mainclassSyntaxFile, mjProject, blackhole,
                                "editor open new refactor syntax/mainclass.sdf3");
                            mainclassSyntaxText = "module mainclass\n" +
                                "\n" +
                                "imports\n" +
                                "\n" +
                                "  lex\n" +
                                "\n" +
                                "context-free syntax\n" +
                                "\n" +
                                "  MainClass.MainClass = <class <ID> { public static void main (String[] <ID>) { <Statement> } }>\n" +
                                "\n" +
                                "template options\n" +
                                "\n" +
                                "  keyword -/- [A-Za-z]\n" +
                                "  tokenize : \"[()\"\n";
                            execEditor(mainclassSyntaxText, mainclassSyntaxFile, mjProject, blackhole,
                                "editor change new refactor syntax/mainclass.sdf3");
                            write(mainclassSyntaxFile, mainclassSyntaxText);

                            // Exec changes
                            execPathChanges(syntaxFile, blackhole, "file change/create refactor syntax/{minijava,mainclass}.sdf3");
                        }

                        // Names and types specification.
                        {
                            // Open editor.
                            final PPath natsFile = calcProject.resolve("nats/calc.nabl2");
                            String natsText = read(natsFile);
                            final String originalNatsText = natsText;
                            execEditor(natsText, natsFile, calcProject, blackhole, "editor open nats/calc.nabl2");
                            // Change editor text.
                            natsText = natsText.replace("{x} <- s_nxt", "{x} <- s");
                            execEditor(natsText, natsFile, calcProject, blackhole, "editor change nats/calc.nabl2");
                            // Save file.
                            write(natsFile, natsText);
                            execPathChanges(natsFile, blackhole, "write file nats/calc.nabl2");
                            // Change back and save.
                            execEditor(originalNatsText, natsFile, calcProject, blackhole, "editor change undo nats/calc.nabl2");
                            write(natsFile, originalNatsText);
                            execPathChanges(natsFile, blackhole, "file change undo nats/calc.nabl2");
                        }

                        // Refactor name and type specification: move into new file.
                        {
                            // Remove statement rule in variables.nabl2
                            final PPath varFile = tigerProject.resolve("nats/variables.nabl2");
                            String varText = read(varFile);
                            execEditor(varText, varFile, tigerProject, blackhole, "editor open refactor nats/variables.nabl2");
                            varText = varText.replace(
                                "[[ Assign(e1, e2) ^ (s) : UNIT() ]] := [[ e1 ^ (s) : ty1 ]], [[ e2 ^ (s) : ty2 ]], ty2 <? ty1 | error $[type mismatch got [ty2] where [ty1] expected] @ e2.",
                                "");
                            execEditor(varText, varFile, tigerProject, blackhole, "editor change 1 refactor nats/variables.nabl2");
                            // Add import from variables.nabl2 to statement
                            varText = varText.replace("imports base", "imports base\nimports statement");
                            execEditor(varText, varFile, tigerProject, blackhole, "editor change 2 refactor nats/variables.nabl2");
                            write(varFile, varText);

                            // Add to statement.nabl2
                            final PPath statFile = tigerProject.resolve("nats/statement.nabl2");
                            String statText = read(statFile);
                            execEditor(statText, statFile, tigerProject, blackhole, "editor open new refactor nats/statement.nabl2");
                            statText = "module statement\n" +
                                "\n" +
                                "rules // statements\n" +
                                "\n" +
                                "  [[ Assign(e1, e2) ^ (s) : UNIT() ]] := [[ e1 ^ (s) : ty1 ]], [[ e2 ^ (s) : ty2 ]], ty2 <? ty1 | error $[type mismatch got [ty2] where [ty1] expected] @ e2.\n";
                            execEditor(statText, statFile, tigerProject, blackhole, "editor change new refactor nats/statement.nabl2");
                            write(statFile, statText);

                            // Exec changes
                            execPathChanges(blackhole, "file change/create refactor nats/{variables,statement}.nabl2", varFile, statFile);
                        }

                        // Extrema change: noop
                        execPathChanges(blackhole, "noop 1");
                        execPathChanges(blackhole, "noop 2");

                        // Extrema change: everything
                        try {
                            final HashSet<PPath> changedPaths = new HashSet<>();
                            root.walk(PPaths.extensionsPathWalker(Lists.newArrayList(
                                "cfg", "esv", "sdf3", "nabl2", "str", "calc", "mj", "tig"
                            ))).forEach(path -> {
                                String text = read(path);
                                text = text + " ";
                                write(path, text);
                                changedPaths.add(path);
                            });
                            execPathChanges(changedPaths, blackhole, "all files changed");
                        } catch(IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
            }
        };

        public abstract ChangeMaker createChangeMaker(PPath root, Blackhole blackhole);
    }


    private static void run(PPath cwd, String... args) {
        try {
            final ProcessBuilder builder = new ProcessBuilder(args);
            builder.directory(cwd.getJavaPath().toFile());
            final Process process = builder.start();
            process.waitFor();
        } catch(InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
