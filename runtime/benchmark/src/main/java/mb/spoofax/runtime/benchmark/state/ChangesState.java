package mb.spoofax.runtime.benchmark.state;

import mb.spoofax.runtime.benchmark.state.exec.BUTopsortState;
import mb.spoofax.runtime.benchmark.state.exec.TDState;
import mb.vfs.path.PPath;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.regex.Pattern;


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


    public void apply(TDState state, Blackhole blackhole) {
        changesKind.apply(state, root, blackhole);
    }

    public void apply(BUTopsortState state, Blackhole blackhole) {
        changesKind.apply(state, root, blackhole);
    }


    @Param({"micro"}) public ChangesKind changesKind;

    public enum ChangesKind {
        micro {
            private final Pattern disableParsingPattern =
                Pattern.compile("parse \\{ .*? \\}", Pattern.MULTILINE | Pattern.DOTALL);

            @Override
            public void apply(TDState state, PPath root, Blackhole blackhole) {
                // Initial execution.
                final PPath project = root.resolve("characters");
                state.execAll(blackhole);

                // Example program.
                // Open editor.
                final PPath programFile = project.resolve("example.chr");
                String programText = read(programFile);
                state.addOrUpdateEditor(programText, programFile, project, blackhole);
                // Change editor text.
                programText = programText + "defg";
                state.addOrUpdateEditor(programText, programFile, project, blackhole);
                // Save file.
                write(programFile, programText);
                state.execAll(blackhole);

                // Syntax styling specification.
                // Open editor.
                final PPath stylingFile = project.resolve("main.esv");
                String stylingText = read(stylingFile);
                state.addOrUpdateEditor(stylingText, stylingFile, project, blackhole);
                // Change editor text.
                stylingText = stylingText.replace("0 0 255", "0 0 255 255 0 0");
                state.addOrUpdateEditor(stylingText, stylingFile, project, blackhole);
                // Save file.
                write(stylingFile, stylingText);
                state.execAll(blackhole);

                // Syntax specification.
                // Open editor.
                final PPath syntaxFile = project.resolve("main.sdf3");
                String syntaxText = read(syntaxFile);
                state.addOrUpdateEditor(syntaxText, syntaxFile, project, blackhole);
                // Change editor text.
                syntaxText = syntaxText.replace("a-zA-Z", "x-z");
                state.addOrUpdateEditor(syntaxText, syntaxFile, project, blackhole);
                // Save file.
                write(syntaxFile, syntaxText);
                state.execAll(blackhole);

                // Language specification.
                // Open editor.
                final PPath langspecFile = project.resolve("langspec.cfg");
                String langspecText = read(langspecFile);
                state.addOrUpdateEditor(langspecText, langspecFile, project, blackhole);
                // Change editor text: change file extension.
                langspecText = langspecText.replace("file extensions: chr", "file extensions: nope");
                state.addOrUpdateEditor(langspecText, langspecFile, project, blackhole);
                // Save file.
                write(langspecFile, langspecText);
                state.execAll(blackhole);
                // Change editor text: change file extension back.
                langspecText = langspecText.replace("file extensions: nope", "file extensions: chr");
                state.addOrUpdateEditor(langspecText, langspecFile, project, blackhole);
                // Save file.
                write(langspecFile, langspecText);
                state.execAll(blackhole);
                // Change editor text: disable parsing
                langspecText = disableParsingPattern.matcher(langspecText).replaceFirst("");
                state.addOrUpdateEditor(langspecText, langspecFile, project, blackhole);
                // Save file.
                write(langspecFile, langspecText);
                state.execAll(blackhole);
            }

            @Override
            public void apply(BUTopsortState state, PPath root, Blackhole blackhole) {
                // Initial execution.
                final PPath project = root.resolve("characters");
                state.addProject(project, blackhole);

                // Example program.
                // Open editor.
                final PPath programFile = project.resolve("example.chr");
                String programText = read(programFile);
                state.addOrUpdateEditor(programText, programFile, project, blackhole);
                // Change editor text.
                programText = programText + "defg";
                state.addOrUpdateEditor(programText, programFile, project, blackhole);
                // Save file.
                write(programFile, programText);
                state.execPathChanges(Arrays.asList(programFile));

                // Syntax styling specification.
                // Open editor.
                final PPath stylingFile = project.resolve("main.esv");
                String stylingText = read(stylingFile);
                state.addOrUpdateEditor(stylingText, stylingFile, project, blackhole);
                // Change editor text.
                stylingText = stylingText.replace("0 0 255", "0 0 255 255 0 0");
                state.addOrUpdateEditor(stylingText, stylingFile, project, blackhole);
                // Save file.
                write(stylingFile, stylingText);
                state.execPathChanges(Arrays.asList(stylingFile));

                // Syntax specification.
                // Open editor.
                final PPath syntaxFile = project.resolve("main.sdf3");
                String syntaxText = read(syntaxFile);
                state.addOrUpdateEditor(syntaxText, syntaxFile, project, blackhole);
                // Change editor text.
                syntaxText = syntaxText.replace("a-zA-Z", "x-z");
                state.addOrUpdateEditor(syntaxText, syntaxFile, project, blackhole);
                // Save file.
                write(syntaxFile, syntaxText);
                state.execPathChanges(Arrays.asList(syntaxFile));

                // Language specification.
                // Open editor.
                final PPath langspecFile = project.resolve("langspec.cfg");
                String langspecText = read(langspecFile);
                state.addOrUpdateEditor(langspecText, langspecFile, project, blackhole);
                // Change editor text: change file extension.
                langspecText = langspecText.replace("file extensions: chr", "file extensions: nope");
                state.addOrUpdateEditor(langspecText, langspecFile, project, blackhole);
                // Save file.
                write(langspecFile, langspecText);
                state.execPathChanges(Arrays.asList(langspecFile));
                // Change editor text: change file extension back.
                langspecText = langspecText.replace("file extensions: nope", "file extensions: chr");
                state.addOrUpdateEditor(langspecText, langspecFile, project, blackhole);
                // Save file.
                write(langspecFile, langspecText);
                state.execPathChanges(Arrays.asList(langspecFile));
                // Change editor text: disable parsing
                langspecText = disableParsingPattern.matcher(langspecText).replaceFirst("");
                state.addOrUpdateEditor(langspecText, langspecFile, project, blackhole);
                // Save file.
                write(langspecFile, langspecText);
                state.execPathChanges(Arrays.asList(langspecFile));
            }
        };

        public abstract void apply(TDState state, PPath root, Blackhole blackhole);

        public abstract void apply(BUTopsortState state, PPath root, Blackhole blackhole);
    }


    private static String read(PPath file) {
        try {
            return new String(file.readAllBytes());
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void write(PPath file, String text) {
        try {
            Files.write(file.getJavaPath(), text.getBytes());
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
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
