package mb.spoofax.legacy;

import mb.fs.java.JavaFSNode;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageComponent;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class LoadMetaLanguages {
    public static LoadedMetaLanguages loadedMetaLanguages = null;


    public static void loadAll(JavaFSNode root) throws IOException, MetaborgException {
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

    private static JavaFSNode unpackResource(String resource, JavaFSNode root) throws IOException {
        final ClassLoader classLoader = LoadedMetaLanguages.class.getClassLoader();
        final @Nullable URL url = classLoader.getResource(resource);
        if(url == null) {
            throw new IOException("Cannot get resource " + resource + " from class loader " + classLoader);
        }

        final JavaFSNode targetDir = root.appendSegment("." + resource);
        targetDir.delete(true);
        targetDir.createDirectory(true);

        try(
            final InputStream inputStream = url.openStream();
            final ZipInputStream zip = new ZipInputStream(inputStream)
        ) {
            while(true) {
                final ZipEntry entry = zip.getNextEntry();
                if(entry == null) break;
                final JavaFSNode path = targetDir.appendSegment(entry.getName()); // TODO: validate path?
                if(entry.isDirectory()) {
                    path.createDirectory(true);
                } else {
                    final int bufferSize = 8192;
                    try(
                        final OutputStream outputStream = path.newOutputStream();
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

    private static ILanguageComponent load(JavaFSNode dir) throws MetaborgException {
        return LanguageLoader.loadLanguage(dir);
    }
}
