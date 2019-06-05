package mb.tiger.intellij;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import mb.spoofax.intellij.psi.SpoofaxFileImpl;

public final class TigerFileImpl extends SpoofaxFileImpl implements TigerFile {
    public TigerFileImpl(FileViewProvider provider) {
        super(TigerTokenTypes.TIGER_FILE, provider);
    }

    @Override public FileType getFileType() {
        return TigerFileType.instance;
    }
}
