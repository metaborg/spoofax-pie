package mb.spoofax.legacy;

import mb.pie.vfs.path.PPath;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageComponent;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class LoadMetaLanguages {
    public static LoadedMetaLanguages loadedMetaLanguages = null;


    public static void loadAll(PPath root) throws IOException, MetaborgException {
        final String resourceDir = "spoofax_meta_languages";
        final ILanguageComponent config = load(unpackResource(resourceDir + "/spoofax.lang.cfg.spoofax-language", root));
        final ILanguageComponent spoofaxLib = load(unpackResource(resourceDir + "/spoofax.lib.spoofax-language", root));
        final ILanguageComponent esv = load(unpackResource(resourceDir + "/esv.spoofax-language", root));
        final ILanguageComponent stratego = load(unpackResource(resourceDir + "/stratego.spoofax-language", root));
        final ILanguageComponent sdf3 = load(unpackResource(resourceDir + "/sdf3.spoofax-language", root));
        final ILanguageComponent nabl2Lang = load(unpackResource(resourceDir + "/nabl2.lang.spoofax-language", root));
        final ILanguageComponent nabl2Shared = load(unpackResource(resourceDir + "/nabl2.shared.spoofax-language", root));
        final ILanguageComponent nabl2Runtime = load(unpackResource(resourceDir + "/nabl2.runtime.spoofax-language", root));
        loadedMetaLanguages = new LoadedMetaLanguages(config, spoofaxLib, esv, stratego, sdf3, nabl2Lang, nabl2Shared, nabl2Runtime);
    }

    private static PPath unpackResource(String resource, PPath root) throws IOException {
        final PPath targetDir = root.resolve("." + resource);
        targetDir.deleteAll();
        targetDir.createDirectories();
        try(
            final InputStream inputStream = LoadedMetaLanguages.class.getClassLoader().getResourceAsStream(resource);
            final ZipInputStream zip = new ZipInputStream(inputStream)
        ) {
            while(true) {
                final ZipEntry entry = zip.getNextEntry();
                if(entry == null) break;
                final PPath path = targetDir.resolve(entry.getName()); // TODO: validate path?
                if(entry.isDirectory()) {
                    path.createDirectories();
                } else {
                    final int bufferSize = 8192;
                    try(
                        final OutputStream outputStream = path.outputStream();
                        final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, bufferSize)
                    ) {
                        final byte[] buffer = new byte[bufferSize];
                        while(true) {
                            final int readBytes = zip.read(buffer, 0, bufferSize);
                            if(readBytes == -1) break;
                            bufferedOutputStream.write(buffer, 0, readBytes);
                        }
                    }
                }
                zip.closeEntry();
            }
        }
        return targetDir;
    }

    private static ILanguageComponent load(PPath dir) throws MetaborgException {
        return LanguageLoader.loadLanguage(dir);
    }
}
