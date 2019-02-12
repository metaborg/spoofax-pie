package mb.spoofax.pie.benchmark.state;

import mb.fs.api.node.match.PathNodeMatcher;
import mb.fs.api.node.walk.AllNodeWalker;
import mb.fs.api.path.match.ExtensionsPathMatcher;
import mb.fs.java.JavaFSPath;
import mb.spoofax.pie.benchmark.ChangeMaker;
import mb.spoofax.pie.benchmark.state.exec.BUState;
import mb.spoofax.pie.benchmark.state.exec.TDState;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.util.HashSet;


@State(Scope.Benchmark)
public class ChangesState {
    private JavaFSPath root;


    public void setup(WorkspaceState workspaceState) {
        this.root = workspaceState.root;
    }

    public void reset(WorkspaceState workspaceState) {
        run(root, "git", "reset", "--hard");
        run(root, "git", "clean", "-ddffxx");
        workspaceState.unpackAndLoadMetaLanguages();
    }


    public void exec(TDState state, Blackhole blackhole) {
        final ChangeMaker changeMaker = changesKind.createChangeMaker(root, blackhole);
        changeMaker.run(state);
    }

    public void exec(BUState state, Blackhole blackhole) {
        final ChangeMaker changeMaker = changesKind.createChangeMaker(root, blackhole);
        changeMaker.run(state);
    }


    @Param({"."}) public String changesCsvDir;

    @Param({"spoofax_pie_test_workspace_languages"}) public ChangesKind changesKind;

    @SuppressWarnings("unused") public enum ChangesKind {
        spoofax_pie_test_workspace_languages {
            @Override public ChangeMaker createChangeMaker(JavaFSPath root, Blackhole blackhole) {
                return new ChangeMaker() {
                    @Override protected void apply() {
                        // Initial execution: add and execute project tasks.
                        final JavaFSPath calcProject = root.appendSegment("calc");
                        addOrUpdateProject(calcProject, blackhole, "add project 'calc'");
                        final JavaFSPath tigerProject = root.appendSegment("tiger");
                        addOrUpdateProject(tigerProject, blackhole, "add project 'tiger'");
                        final JavaFSPath charsProject = root.appendSegment("chars");
                        addOrUpdateProject(charsProject, blackhole, "add project 'chars'");

                        // Example program.
                        {
                            // Open editor.
                            final JavaFSPath programFile = calcProject.appendSegment("example/basic/gt.calc");
                            String programText = read(programFile);
                            addOrUpdateEditor(programText, programFile, calcProject, blackhole, "editor open calc/example/basic/gt.calc");
                            // Change editor text.
                            programText = "4 > 5";
                            addOrUpdateEditor(programText, programFile, calcProject, blackhole, "editor change 1 calc/example/basic/gt.calc");
                            programText = "4 > ";
                            addOrUpdateEditor(programText, programFile, calcProject, blackhole, "editor change 2 calc/example/basic/gt.calc");
                            programText = "4 > 2;";
                            addOrUpdateEditor(programText, programFile, calcProject, blackhole, "editor change 3 calc/example/basic/gt.calc");
                            // Save file.
                            write(programFile, programText);
                            execResourceChanges(programFile, blackhole, "file change calc/example/basic/gt.calc");
                        }

                        // Add new example program.
                        {
                            // Open editor.
                            final JavaFSPath programFile = calcProject.appendSegment("example/basic/lt.calc");
                            String programText = "";
                            addOrUpdateEditor(programText, programFile, calcProject, blackhole, "editor open new calc/example/basic/lt.calc");
                            // Change editor text.
                            programText = "0 < 1;";
                            addOrUpdateEditor(programText, programFile, calcProject, blackhole, "editor change new calc/example/basic/lt.calc");
                            // Save file.
                            write(programFile, programText);
                            execResourceChanges(programFile, blackhole, "file create calc/example/basic/lt.calc");
                        }

                        // Edit multiple example programs.
                        {
                            final JavaFSPath programFile1 = tigerProject.appendSegment("example/appel/test01.tig");
                            String programText1 = read(programFile1);
                            addOrUpdateEditor(programText1, programFile1, tigerProject, blackhole, "editor open tiger/example/appel/test01.tig");
                            final JavaFSPath programFile2 = tigerProject.appendSegment("example/appel/test02.tig");
                            String programText2 = read(programFile2);
                            addOrUpdateEditor(programText2, programFile2, tigerProject, blackhole, "editor open tiger/example/appel/test02.tig");
                            final JavaFSPath programFile3 = tigerProject.appendSegment("example/appel/test03.tig");
                            String programText3 = read(programFile3);
                            addOrUpdateEditor(programText3, programFile3, tigerProject, blackhole, "editor open tiger/example/appel/test03.tig");

                            programText1 = programText1.replace("arrtype [10]", "arrtype [20]");
                            addOrUpdateEditor(programText1, programFile1, tigerProject, blackhole, "editor change tiger/example/appel/test01.tig");
                            programText2 = programText2.replace("arrtype [10]", "arrtype [20]");
                            addOrUpdateEditor(programText2, programFile2, tigerProject, blackhole, "editor change tiger/example/appel/test02.tig");
                            programText3 = programText3.replace("Somebody", "Everyone");
                            addOrUpdateEditor(programText3, programFile3, tigerProject, blackhole, "editor change tiger/example/appel/test03.tig");

                            write(programFile1, programText1);
                            write(programFile2, programText2);
                            write(programFile3, programText3);
                            execResourceChanges(blackhole, "file change tiger/example/appel/{test01.tig,test02.tig,test03.tig}", programFile1,
                                programFile2, programFile3);
                        }
                    }
                };
            }
        },
        spoofax_pie_experiment_medium {
            @Override public ChangeMaker createChangeMaker(JavaFSPath root, Blackhole blackhole) {
                return new ChangeMaker() {
                    @Override protected void apply() {
                        // Initial execution: add and execute project tasks.
                        final JavaFSPath calcProject = root.appendSegment("lang.calc");
                        addOrUpdateProject(calcProject, blackhole, "initial lang.calc");
                        final JavaFSPath tigerProject = root.appendSegment("lang.tiger");
                        addOrUpdateProject(tigerProject, blackhole, "initial lang.tiger");
                        final JavaFSPath mjProject = root.appendSegment("lang.minijava");
                        addOrUpdateProject(mjProject, blackhole, "initial lang.minijava");

                        // Example program.
                        {
                            // Open editor.
                            final JavaFSPath programFile = calcProject.appendSegment("example/basic/gt.calc");
                            String programText = read(programFile);
                            addOrUpdateEditor(programText, programFile, calcProject, blackhole, "editor open example/basic/gt.calc");
                            // Change editor text.
                            programText = "4 > 5";
                            addOrUpdateEditor(programText, programFile, calcProject, blackhole, "editor change 1 example/basic/gt.calc");
                            programText = "4 > ";
                            addOrUpdateEditor(programText, programFile, calcProject, blackhole, "editor change 2 example/basic/gt.calc");
                            programText = "4 > 2;";
                            addOrUpdateEditor(programText, programFile, calcProject, blackhole, "editor change 3 example/basic/gt.calc");
                            // Save file.
                            write(programFile, programText);
                            execResourceChanges(programFile, blackhole, "file change example/basic/gt.calc");
                        }

                        // Add new example program.
                        {
                            // Open editor.
                            final JavaFSPath programFile = calcProject.appendSegment("example/basic/lt.calc");
                            String programText = "";
                            addOrUpdateEditor(programText, programFile, calcProject, blackhole, "editor open new example/basic/lt.calc");
                            // Change editor text.
                            programText = "0 < 1;";
                            addOrUpdateEditor(programText, programFile, calcProject, blackhole, "editor change new example/basic/lt.calc");
                            // Save file.
                            write(programFile, programText);
                            execResourceChanges(programFile, blackhole, "file create example/basic/lt.calc");
                        }

                        // Edit multiple example programs.
                        {
                            final JavaFSPath programFile1 = tigerProject.appendSegment("example/appel/test01.tig");
                            String programText1 = read(programFile1);
                            addOrUpdateEditor(programText1, programFile1, tigerProject, blackhole, "editor open example/appel/test01.tig");
                            final JavaFSPath programFile2 = tigerProject.appendSegment("example/appel/test02.tig");
                            String programText2 = read(programFile2);
                            addOrUpdateEditor(programText2, programFile2, tigerProject, blackhole, "editor open example/appel/test02.tig");
                            final JavaFSPath programFile3 = tigerProject.appendSegment("example/appel/test03.tig");
                            String programText3 = read(programFile3);
                            addOrUpdateEditor(programText3, programFile3, tigerProject, blackhole, "editor open example/appel/test03.tig");

                            programText1 = programText1.replace("arrtype [10]", "arrtype [20]");
                            addOrUpdateEditor(programText1, programFile1, tigerProject, blackhole, "editor change example/appel/test01.tig");
                            programText2 = programText2.replace("arrtype [10]", "arrtype [20]");
                            addOrUpdateEditor(programText2, programFile2, tigerProject, blackhole, "editor change example/appel/test02.tig");
                            programText3 = programText3.replace("Somebody", "Everyone");
                            addOrUpdateEditor(programText3, programFile3, tigerProject, blackhole, "editor change example/appel/test03.tig");

                            write(programFile1, programText1);
                            write(programFile2, programText2);
                            write(programFile3, programText3);
                            execResourceChanges(blackhole, "file change example/appel/{test01.tig,test02.tig,test03.tig}", programFile1,
                                programFile2, programFile3);
                        }

                        // Syntax styling specification.
                        {
                            // Open editor.
                            final JavaFSPath stylingFile = calcProject.appendSegment("style/style.esv");
                            String stylingText = read(stylingFile);
                            addOrUpdateEditor(stylingText, stylingFile, calcProject, blackhole, "editor open style/style.esv");
                            // Change editor text.
                            stylingText = stylingText.replace("keyword    : 127 0 85 bold", "keyword    : ");
                            addOrUpdateEditor(stylingText, stylingFile, calcProject, blackhole, "editor change 1 style/style.esv");
                            stylingText = stylingText.replace("keyword    : ", "keyword    : 255 0 0");
                            addOrUpdateEditor(stylingText, stylingFile, calcProject, blackhole, "editor change 2 style/style.esv");
                            stylingText = stylingText.replace("keyword    : 255 0 0", "keyword    : 255 0 0 0 255 0");
                            addOrUpdateEditor(stylingText, stylingFile, calcProject, blackhole, "editor change 3 style/style.esv");
                            // Save file.
                            write(stylingFile, stylingText);
                            execResourceChanges(stylingFile, blackhole, "file change style/style.esv");
                        }

                        // Add styling specification.
                        {
                            // Open syntax styling editor.
                            final JavaFSPath stylingFile = tigerProject.appendSegment("style/style.esv");
                            String stylingText = "";
                            addOrUpdateEditor(stylingText, stylingFile, tigerProject, blackhole, "editor open new style/style.esv");
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
                            addOrUpdateEditor(stylingText, stylingFile, tigerProject, blackhole, "editor change new style/style.esv");
                            // Write new file.
                            write(stylingFile, stylingText);
                            execResourceChanges(stylingFile, blackhole, "file create style/style.esv");
                        }

                        // Edit multiple styling specifications.
                        {
                            // Calc
                            final JavaFSPath calcStylingFile = calcProject.appendSegment("style/style.esv");
                            String calcStylingText = read(calcStylingFile);
                            addOrUpdateEditor(calcStylingText, calcStylingFile, calcProject, blackhole, "editor open lang.calc/style/style.esv");
                            calcStylingText = calcStylingText.replace("number     : 0 127 0", "number     : 0 127 127");
                            addOrUpdateEditor(calcStylingText, calcStylingFile, calcProject, blackhole, "editor change lang.calc/style/style.esv");
                            write(calcStylingFile, calcStylingText);
                            // Tiger
                            final JavaFSPath tigStylingFile = tigerProject.appendSegment("style/style.esv");
                            String tigStylingText = read(tigStylingFile);
                            addOrUpdateEditor(tigStylingText, tigStylingFile, tigerProject, blackhole, "editor open lang.tiger/style/style.esv");
                            tigStylingText = tigStylingText.replace("number     : 0 127 0", "number     : 0 127 127");
                            addOrUpdateEditor(tigStylingText, tigStylingFile, tigerProject, blackhole, "editor change lang.tiger/style/style.esv");
                            write(tigStylingFile, tigStylingText);
                            // Exec changes
                            execResourceChanges(blackhole, "file change {lang.calc,lang.tiger}/style/style.esv", calcStylingFile,
                                tigStylingFile);
                        }

                        // Add minijava language.
                        {
                            final JavaFSPath langSpecFile = mjProject.appendSegment("langspec.cfg");
                            String langSpecText = "";
                            addOrUpdateEditor(langSpecText, langSpecFile, mjProject, blackhole, "editor open new lang.minijava/langspec.cfg");
                            langSpecText = "langspec {\n" +
                                "  identification {\n" +
                                "    id: \"org.metaborg:minijava.lang\"\n" +
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
                                "      include dirs: trans, nats\n" +
                                "    }\n" +
                                "    stratego strategy id: nats\n" +
                                "    root scope per file: true\n" +
                                "  }\n" +
                                "}\n";
                            addOrUpdateEditor(langSpecText, langSpecFile, mjProject, blackhole, "editor change new lang.minijava/langspec.cfg");
                            write(langSpecFile, langSpecText);
                            execResourceChanges(langSpecFile, blackhole, "file create lang.minijava/langspec.cfg");
                        }

                        // Syntax specification: small/local change.
                        {
                            // Open editor.
                            final JavaFSPath syntaxFile = calcProject.appendSegment("syntax/CalcLexical.sdf3");
                            String syntaxText = read(syntaxFile);
                            final String originalSyntaxText = syntaxText;
                            addOrUpdateEditor(syntaxText, syntaxFile, calcProject, blackhole, "editor open syntax/CalcLexical.sdf3");
                            // Change editor text.
                            syntaxText = syntaxText.replace("INT      = \"-\"? [0-9]+", "INT      = \"-\"? [8-9]+");
                            addOrUpdateEditor(syntaxText, syntaxFile, calcProject, blackhole, "editor change small syntax/CalcLexical.sdf3");
                            // Save file.
                            write(syntaxFile, syntaxText);
                            execResourceChanges(syntaxFile, blackhole, "file change small syntax/CalcLexical.sdf3");
                            // Change back and save.
                            addOrUpdateEditor(originalSyntaxText, syntaxFile, calcProject, blackhole,
                                "editor change small undo syntax/CalcLexical.sdf3");
                            write(syntaxFile, originalSyntaxText);
                            execResourceChanges(syntaxFile, blackhole, "file change small undo syntax/CalcLexical.sdf3");
                        }

                        // Syntax specification: cascading change.
                        {
                            // Open editor.
                            final JavaFSPath syntaxFile = calcProject.appendSegment("syntax/Calc.sdf3");
                            String syntaxText = read(syntaxFile);
                            final String originalSyntaxText = syntaxText;
                            addOrUpdateEditor(syntaxText, syntaxFile, calcProject, blackhole, "editor open cascading syntax/Calc.sdf3");
                            // Change editor text.
                            syntaxText = syntaxText.replace("Exp.Num = NUM", "Exp.Num = ID");
                            addOrUpdateEditor(syntaxText, syntaxFile, calcProject, blackhole, "editor change cascading syntax/Calc.sdf3");
                            // Save file.
                            write(syntaxFile, syntaxText);
                            execResourceChanges(syntaxFile, blackhole, "file change cascading syntax/Calc.sdf3");
                            // Change back and save.
                            addOrUpdateEditor(originalSyntaxText, syntaxFile, calcProject, blackhole,
                                "editor change cascading undo syntax/Calc.sdf3");
                            write(syntaxFile, originalSyntaxText);
                            execResourceChanges(syntaxFile, blackhole, "file change cascading undo syntax/Calc.sdf3");
                        }

                        // Refactor syntax specification: move into new file.
                        {
                            final JavaFSPath syntaxFile = mjProject.appendSegment("syntax/minijava.sdf3");
                            final JavaFSPath mainclassSyntaxFile = mjProject.appendSegment("syntax/mainclass.sdf3");

                            // Remove main class rule from minijava.sdf3
                            String syntaxText = read(syntaxFile);
                            addOrUpdateEditor(syntaxText, syntaxFile, mjProject, blackhole, "editor open refactor syntax/minijava.sdf3");
                            syntaxText = syntaxText.replace(
                                "MainClass.MainClass = <class <ID> { public static void main (String[] <ID>) { <Statement> } }>",
                                ""
                            );
                            addOrUpdateEditor(syntaxText, syntaxFile, mjProject, blackhole, "editor change 1 refactor syntax/minijava.sdf3");
                            // Add import from minijava.sdf3 to mainclass.sdf3
                            syntaxText = syntaxText.replace("classes", "classes mainclass");
                            addOrUpdateEditor(syntaxText, syntaxFile, mjProject, blackhole, "editor change 2 refactor syntax/minijava.sdf3");
                            write(syntaxFile, syntaxText);

                            // Add to mainclass.sdf3
                            String mainclassSyntaxText = read(mainclassSyntaxFile);
                            addOrUpdateEditor(mainclassSyntaxText, mainclassSyntaxFile, mjProject, blackhole,
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
                            addOrUpdateEditor(mainclassSyntaxText, mainclassSyntaxFile, mjProject, blackhole,
                                "editor change new refactor syntax/mainclass.sdf3");
                            write(mainclassSyntaxFile, mainclassSyntaxText);

                            // Exec changes
                            execResourceChanges(syntaxFile, blackhole, "file change/create refactor syntax/{minijava,mainclass}.sdf3");
                        }

                        // Names and types specification.
                        {
                            // Open editor.
                            final JavaFSPath natsFile = calcProject.appendSegment("nats/calc.nabl2");
                            String natsText = read(natsFile);
                            final String originalNatsText = natsText;
                            addOrUpdateEditor(natsText, natsFile, calcProject, blackhole, "editor open nats/calc.nabl2");
                            // Change editor text.
                            natsText = natsText.replace("{x} <- s_nxt", "{x} <- s");
                            addOrUpdateEditor(natsText, natsFile, calcProject, blackhole, "editor change nats/calc.nabl2");
                            // Save file.
                            write(natsFile, natsText);
                            execResourceChanges(natsFile, blackhole, "write file nats/calc.nabl2");
                            // Change back and save.
                            addOrUpdateEditor(originalNatsText, natsFile, calcProject, blackhole, "editor change undo nats/calc.nabl2");
                            write(natsFile, originalNatsText);
                            execResourceChanges(natsFile, blackhole, "file change undo nats/calc.nabl2");
                        }

                        // Refactor name and type specification: move into new file.
                        {
                            // Remove statement rule in variables.nabl2
                            final JavaFSPath varFile = tigerProject.appendSegment("nats/variables.nabl2");
                            String varText = read(varFile);
                            addOrUpdateEditor(varText, varFile, tigerProject, blackhole, "editor open refactor nats/variables.nabl2");
                            varText = varText.replace(
                                "[[ Assign(e1, e2) ^ (s) : UNIT() ]] := [[ e1 ^ (s) : ty1 ]], [[ e2 ^ (s) : ty2 ]], ty2 <? ty1 | error $[type mismatch got [ty2] where [ty1] expected] @ e2.",
                                "");
                            addOrUpdateEditor(varText, varFile, tigerProject, blackhole, "editor change 1 refactor nats/variables.nabl2");
                            // Add import from variables.nabl2 to statement
                            varText = varText.replace("imports base", "imports base\nimports statement");
                            addOrUpdateEditor(varText, varFile, tigerProject, blackhole, "editor change 2 refactor nats/variables.nabl2");
                            write(varFile, varText);

                            // Add to statement.nabl2
                            final JavaFSPath statFile = tigerProject.appendSegment("nats/statement.nabl2");
                            String statText = read(statFile);
                            addOrUpdateEditor(statText, statFile, tigerProject, blackhole, "editor open new refactor nats/statement.nabl2");
                            statText = "module statement\n" +
                                "\n" +
                                "rules // statements\n" +
                                "\n" +
                                "  [[ Assign(e1, e2) ^ (s) : UNIT() ]] := [[ e1 ^ (s) : ty1 ]], [[ e2 ^ (s) : ty2 ]], ty2 <? ty1 | error $[type mismatch got [ty2] where [ty1] expected] @ e2.\n";
                            addOrUpdateEditor(statText, statFile, tigerProject, blackhole, "editor change new refactor nats/statement.nabl2");
                            write(statFile, statText);

                            // Exec changes
                            execResourceChanges(blackhole, "file change/create refactor nats/{variables,statement}.nabl2", varFile, statFile);
                        }

                        // Extrema change: noop
                        execResourceChanges(blackhole, "noop 1");
                        execResourceChanges(blackhole, "noop 2");

                        // Extrema change: everything
                        try {
                            final HashSet<JavaFSPath> changedPaths = new HashSet<>();
                            root.toNode().walk(new AllNodeWalker(), new PathNodeMatcher(
                                new ExtensionsPathMatcher("cfg", "esv", "sdf3", "nabl2", "str", "calc", "mj", "tig"))).forEach(node -> {
                                String text = read(node);
                                text = text + " ";
                                write(node, text);
                                changedPaths.add(node.getPath());
                            });
                            execResourceChanges(changedPaths, blackhole, "all files changed");
                        } catch(IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
            }
        };

        public abstract ChangeMaker createChangeMaker(JavaFSPath root, Blackhole blackhole);
    }


    private static void run(JavaFSPath cwd, String... args) {
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
