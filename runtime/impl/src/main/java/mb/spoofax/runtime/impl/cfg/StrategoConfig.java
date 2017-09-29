package mb.spoofax.runtime.impl.cfg;

import static mb.spoofax.runtime.impl.term.Terms.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.spoofax.runtime.impl.term.Terms;
import mb.vfs.path.PPath;

@Value.Immutable
@Serial.Version(value = 1)
public interface StrategoConfig extends Serializable {
    @Value.Parameter PPath mainFile();

    @Value.Parameter List<PPath> includeDirs();

    @Value.Parameter List<PPath> includeFiles();

    @Value.Parameter List<String> includeLibs();

    @Value.Parameter PPath baseDir();

    @Value.Parameter PPath cacheDir();

    @Value.Parameter PPath outputFile();


    public static @Nullable ImmutableStrategoConfig fromTerm(IStrategoTerm root, PPath dir) {
        // @formatter:off
        // StrategoConfig([...])
        final IStrategoTerm options = root.getSubterm(0);
        final PPath mainFile = Terms.stream(options)
            .filter((t) -> isAppl(t, "StrategoConfigMainFile")) // StrategoConfigMainFile(Path(...))
            .map((t) -> asString(t.getSubterm(0).getSubterm(0)))
            .map(dir::resolve)
            .findFirst()
            .orElse(null);
        if(mainFile == null) {
            return null;
        }
        final ArrayList<PPath> includeDirs = Terms.stream(options)
            .filter((t) -> isAppl(t, "StrategoConfigIncludeDirs")) // StrategoConfigIncludeDirs(Paths([Path("..."), ...]))
            .flatMap((t) -> stream(t.getSubterm(0).getSubterm(0)))
            .map((t) -> asString(t.getSubterm(0)))
            .map(dir::resolve)
            .collect(Collectors.toCollection(ArrayList::new));
        final ArrayList<PPath> includeFiles = Terms.stream(options)
            .filter((t) -> isAppl(t, "StrategoConfigIncludeFiles")) // StrategoConfigIncludeFiles(Paths([Path("..."), ...]))
            .flatMap((t) -> stream(t.getSubterm(0).getSubterm(0)))
            .map((t) -> asString(t.getSubterm(0)))
            .map(dir::resolve)
            .collect(Collectors.toCollection(ArrayList::new));
        final ArrayList<String> includeLibs = Terms.stream(options)
            .filter((t) -> isAppl(t, "StrategoConfigIncludeLibs")) // StrategoConfigIncludeLibs(Ids([Id("..."), ...]))
            .flatMap((t) -> stream(t.getSubterm(0).getSubterm(0)))
            .map((t) -> asString(t.getSubterm(0)))
            .collect(Collectors.toCollection(ArrayList::new));
        final PPath baseDir = Terms.stream(options)
            .filter((t) -> isAppl(t, "StrategoConfigBaseDir")) // StrategoConfigBaseDir(Path(...))
            .map((t) -> asString(t.getSubterm(0).getSubterm(0)))
            .map(dir::resolve)
            .findFirst()
            .orElse(mainFile.parent()); // Default to parent of main file
        final PPath cacheDir = Terms.stream(options)
            .filter((t) -> isAppl(t, "StrategoConfigCacheDir")) // StrategoConfigCacheDir(Path(...))
            .map((t) -> asString(t.getSubterm(0).getSubterm(0)))
            .map(dir::resolve)
            .findFirst()
            .orElseGet(() -> {
                final PPath parent = mainFile.parent();
                if(parent != null) {
                    // Default to <parent of main file>/target/str-cache
                    return parent.resolve("target/str-cache");
                }
                return null;
            });
        final PPath outputFile = Terms.stream(options)
            .filter((t) -> isAppl(t, "StrategoConfigOutputFile")) // StrategoConfigOutputFile(Path(...))
            .map((t) -> asString(t.getSubterm(0).getSubterm(0)))
            .map(dir::resolve)
            .findFirst()
            .orElse(mainFile.extend(".ctree")); // Default to <main file>.ctree
        // @formatter:on

        return ImmutableStrategoConfig.of(mainFile, includeDirs, includeFiles, includeLibs, baseDir, cacheDir,
            outputFile);
    }
}
