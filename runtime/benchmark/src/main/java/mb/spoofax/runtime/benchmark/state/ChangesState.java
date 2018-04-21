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
import java.util.ArrayList;


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
        debug_syntax_styling_editor_open {
            @Override public ChangeMaker createChangeMaker(PPath root, Blackhole blackhole) {
                return new ChangeMaker() {
                    @Override protected void apply() {
                        // Initial execution.
                        final PPath calcProject = root.resolve("lang.calc");
                        addProject(calcProject, blackhole, "initial lang.calc");
                        execInitial(blackhole, "initial");

                        final PPath stylingFile = calcProject.resolve("style/style.esv");
                        String stylingText = read(stylingFile);
                        execEditor(stylingText, stylingFile, calcProject, blackhole, "editor open style/style.esv");
                    }
                };
            }
        },
        debug_nabl2_write {
            @Override public ChangeMaker createChangeMaker(PPath root, Blackhole blackhole) {
                return new ChangeMaker() {
                    @Override protected void apply() {
                        // Initial execution.
                        final PPath calcProject = root.resolve("lang.calc");
                        addProject(calcProject, blackhole, "initial lang.calc");
                        execInitial(blackhole, "initial");

                        final PPath natsFile = calcProject.resolve("nats/calc.nabl2");
                        String natsText = read(natsFile);
                        natsText = natsText.replace("{x} <- s_nxt", "{x} <- s");
                        write(natsFile, natsText);
                        execPathChanges(natsFile, blackhole, "write file nats/calc.nabl2");
                    }
                };
            }
        },
        debug_sdf3_write {
            @Override public ChangeMaker createChangeMaker(PPath root, Blackhole blackhole) {
                return new ChangeMaker() {
                    @Override protected void apply() {
                        // Initial execution.
                        final PPath calcProject = root.resolve("lang.calc");
                        addProject(calcProject, blackhole, "initial lang.calc");
                        execInitial(blackhole, "initial");

                        final PPath syntaxFile = calcProject.resolve("syntax/CalcLexical.sdf3");
                        String syntaxText = read(syntaxFile);
                        syntaxText = syntaxText.replace("INT      = \"-\"? [0-9]+", "INT      = \"-\"? [8-9]+");
                        write(syntaxFile, syntaxText);
                        execPathChanges(syntaxFile, blackhole, "file change syntax/CalcLexical.sdf3");
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
                        final PPath mjProject = root.resolve("lang.minijava");
                        addProject(mjProject, blackhole, "initial lang.minijava");
                        final PPath tigerProject = root.resolve("lang.tiger");
                        addProject(tigerProject, blackhole, "initial lang.tiger");
                        execInitial(blackhole, "initial");

                        // Example program.
                        // Open editor.
                        final PPath programFile = calcProject.resolve("example/basic/gt.calc");
                        String programText = read(programFile);
                        execEditor(programText, programFile, calcProject, blackhole, "editor open example/basic/gt.calc");
                        // Change editor text.
                        programText = programText.replace("4 > 5;", "2 > 3;");
                        execEditor(programText, programFile, calcProject, blackhole, "editor change example/basic/gt.calc");
                        // Save file.
                        write(programFile, programText);
                        execPathChanges(programFile, blackhole, "file change example/basic/gt.calc");

                        // Syntax styling specification.
                        // Open editor.
                        final PPath stylingFile = calcProject.resolve("style/style.esv");
                        String stylingText = read(stylingFile);
                        execEditor(stylingText, stylingFile, calcProject, blackhole, "editor open style/style.esv");
                        // Change editor text.
                        stylingText = stylingText.replace("127 0 85 bold", "0 0 255 255 0 0");
                        execEditor(stylingText, stylingFile, calcProject, blackhole, "editor change style/style.esv");
                        // Save file.
                        write(stylingFile, stylingText);
                        execPathChanges(stylingFile, blackhole, "file change style/style.esv");

                        // Syntax specification.
                        // Open editor.
                        final PPath syntaxFile = calcProject.resolve("syntax/CalcLexical.sdf3");
                        String syntaxText = read(syntaxFile);
                        final String originalSyntaxText = syntaxText;
                        execEditor(syntaxText, syntaxFile, calcProject, blackhole, "editor open syntax/CalcLexical.sdf3");
                        // Change editor text.
                        syntaxText = syntaxText.replace("INT      = \"-\"? [0-9]+", "INT      = \"-\"? [8-9]+");
                        execEditor(syntaxText, syntaxFile, calcProject, blackhole, "editor change syntax/CalcLexical.sdf3");
                        // Save file.
                        write(syntaxFile, syntaxText);
                        execPathChanges(syntaxFile, blackhole, "file change syntax/CalcLexical.sdf3");
                        // Change back and save.
                        execEditor(originalSyntaxText, syntaxFile, calcProject, blackhole, "editor change undo syntax/CalcLexical.sdf3");
                        write(syntaxFile, originalSyntaxText);
                        execPathChanges(syntaxFile, blackhole, "file change undo syntax/CalcLexical.sdf3");

                        // Names and types specification.
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

                        // Language specification.
                        // Open editor.
//                        final PPath langspecFile = calcProject.resolve("langspec.cfg");
//                        String langspecText = read(langspecFile);
//                        execEditor(langspecText, langspecFile, calcProject, blackhole, "editor open langspec.cfg");
//                        // Change editor text: change file extension.
//                        langspecText = langspecText.replace("file extensions: calc", "file extensions: nope");
//                        execEditor(langspecText, langspecFile, calcProject, blackhole, "editor change langspec.cfg extension");
//                        // Save file.
//                        write(langspecFile, langspecText);
//                        execPathChanges(langspecFile, blackhole, "file change langspec.cfg extension");
//                        // Change editor text: change file extension back.
//                        langspecText = langspecText.replace("file extensions: nope", "file extensions: calc");
//                        execEditor(langspecText, langspecFile, calcProject, blackhole, "editor change undo langspec.cfg extension");
//                        // Save file.
//                        write(langspecFile, langspecText);
//                        execPathChanges(langspecFile, blackhole, "file change undo langspec.cfg extension");

                        // Extrema change: noop
                        execPathChanges(Lists.newArrayList(), blackhole, "noop");

                        // Extrema change: everything
                        try {
                            final ArrayList<PPath> changedPaths = Lists.newArrayList();
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

        //        micro {
//            private final Pattern disableParsingPattern =
//                Pattern.compile("parse \\{ .*? \\}", Pattern.MULTILINE | Pattern.DOTALL);
//
//            @Override
//            public void apply(TDState state, PPath root, Blackhole blackhole) {
//                // Initial execution.
//                final PPath project = root.resolve("characters");
//                state.execAll(blackhole);
//
//                // Example program.
//                // Open editor.
//                final PPath programFile = project.resolve("example.chr");
//                String programText = read(programFile);
//                state.addOrUpdateEditor(programText, programFile, project, blackhole);
//                // Change editor text.
//                programText = programText + "defg";
//                state.addOrUpdateEditor(programText, programFile, project, blackhole);
//                // Save file.
//                write(programFile, programText);
//                state.execAll(blackhole);
//
//                // Syntax styling specification.
//                // Open editor.
//                final PPath stylingFile = project.resolve("main.esv");
//                String stylingText = read(stylingFile);
//                state.addOrUpdateEditor(stylingText, stylingFile, project, blackhole);
//                // Change editor text.
//                stylingText = stylingText.replace("0 0 255", "0 0 255 255 0 0");
//                state.addOrUpdateEditor(stylingText, stylingFile, project, blackhole);
//                // Save file.
//                write(stylingFile, stylingText);
//                state.execAll(blackhole);
//
//                // Syntax specification.
//                // Open editor.
//                final PPath syntaxFile = project.resolve("main.sdf3");
//                String syntaxText = read(syntaxFile);
//                state.addOrUpdateEditor(syntaxText, syntaxFile, project, blackhole);
//                // Change editor text.
//                syntaxText = syntaxText.replace("a-zA-Z", "x-z");
//                state.addOrUpdateEditor(syntaxText, syntaxFile, project, blackhole);
//                // Save file.
//                write(syntaxFile, syntaxText);
//                state.execAll(blackhole);
//
//                // Language specification.
//                // Open editor.
//                final PPath langspecFile = project.resolve("langspec.cfg");
//                String langspecText = read(langspecFile);
//                state.addOrUpdateEditor(langspecText, langspecFile, project, blackhole);
//                // Change editor text: change file extension.
//                langspecText = langspecText.replace("file extensions: chr", "file extensions: nope");
//                state.addOrUpdateEditor(langspecText, langspecFile, project, blackhole);
//                // Save file.
//                write(langspecFile, langspecText);
//                state.execAll(blackhole);
//                // Change editor text: change file extension back.
//                langspecText = langspecText.replace("file extensions: nope", "file extensions: chr");
//                state.addOrUpdateEditor(langspecText, langspecFile, project, blackhole);
//                // Save file.
//                write(langspecFile, langspecText);
//                state.execAll(blackhole);
//                // Change editor text: disable parsing
//                langspecText = disableParsingPattern.matcher(langspecText).replaceFirst("");
//                state.addOrUpdateEditor(langspecText, langspecFile, project, blackhole);
//                // Save file.
//                write(langspecFile, langspecText);
//                state.execAll(blackhole);
//            }
//
//            @Override
//            public void apply(BUTopsortState state, PPath root, Blackhole blackhole) {
//                // Initial execution.
//                final PPath project = root.resolve("characters");
//                state.addProject(project, blackhole);
//
//                // Example program.
//                // Open editor.
//                final PPath programFile = project.resolve("example.chr");
//                String programText = read(programFile);
//                state.addOrUpdateEditor(programText, programFile, project, blackhole);
//                // Change editor text.
//                programText = programText + "defg";
//                state.addOrUpdateEditor(programText, programFile, project, blackhole);
//                // Save file.
//                write(programFile, programText);
//                state.execPathChanges(Arrays.asList(programFile));
//
//                // Syntax styling specification.
//                // Open editor.
//                final PPath stylingFile = project.resolve("main.esv");
//                String stylingText = read(stylingFile);
//                state.addOrUpdateEditor(stylingText, stylingFile, project, blackhole);
//                // Change editor text.
//                stylingText = stylingText.replace("0 0 255", "0 0 255 255 0 0");
//                state.addOrUpdateEditor(stylingText, stylingFile, project, blackhole);
//                // Save file.
//                write(stylingFile, stylingText);
//                state.execPathChanges(Arrays.asList(stylingFile));
//
//                // Syntax specification.
//                // Open editor.
//                final PPath syntaxFile = project.resolve("main.sdf3");
//                String syntaxText = read(syntaxFile);
//                state.addOrUpdateEditor(syntaxText, syntaxFile, project, blackhole);
//                // Change editor text.
//                syntaxText = syntaxText.replace("a-zA-Z", "x-z");
//                state.addOrUpdateEditor(syntaxText, syntaxFile, project, blackhole);
//                // Save file.
//                write(syntaxFile, syntaxText);
//                state.execPathChanges(Arrays.asList(syntaxFile));
//
//                // Language specification.
//                // Open editor.
//                final PPath langspecFile = project.resolve("langspec.cfg");
//                String langspecText = read(langspecFile);
//                state.addOrUpdateEditor(langspecText, langspecFile, project, blackhole);
//                // Change editor text: change file extension.
//                langspecText = langspecText.replace("file extensions: chr", "file extensions: nope");
//                state.addOrUpdateEditor(langspecText, langspecFile, project, blackhole);
//                // Save file.
//                write(langspecFile, langspecText);
//                state.execPathChanges(Arrays.asList(langspecFile));
//                // Change editor text: change file extension back.
//                langspecText = langspecText.replace("file extensions: nope", "file extensions: chr");
//                state.addOrUpdateEditor(langspecText, langspecFile, project, blackhole);
//                // Save file.
//                write(langspecFile, langspecText);
//                state.execPathChanges(Arrays.asList(langspecFile));
//                // Change editor text: disable parsing
//                langspecText = disableParsingPattern.matcher(langspecText).replaceFirst("");
//                state.addOrUpdateEditor(langspecText, langspecFile, project, blackhole);
//                // Save file.
//                write(langspecFile, langspecText);
//                state.execPathChanges(Arrays.asList(langspecFile));
//            }
//        },

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
