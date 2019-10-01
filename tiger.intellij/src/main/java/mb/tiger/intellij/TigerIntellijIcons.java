package mb.tiger.intellij;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;


/**
 * Icons used by the Tiger language.
 */
public final class TigerIntellijIcons {

    private static final Icon fileIcon = IconLoader.getIcon("META-INF/fileIcon.svg");

    /**
     * Gets the Tiger file icon.
     * @return The file icon.
     */
    public static Icon getFileIcon() { return fileIcon; }
}
