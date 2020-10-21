package mb.tiger.intellij;

import com.intellij.psi.FileViewProvider;
import mb.spoofax.intellij.psi.SpoofaxFile;

public class TigerFile extends SpoofaxFile {
    public TigerFile(FileViewProvider provider) {
        super(provider, TigerPlugin.getComponent());
    }
}
