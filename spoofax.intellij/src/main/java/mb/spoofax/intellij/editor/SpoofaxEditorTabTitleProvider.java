package mb.spoofax.intellij.editor;

import com.intellij.ide.FileIconProvider;
import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.fileEditor.UniqueVFilePathBuilder;
import com.intellij.openapi.fileEditor.impl.EditorTabTitleProvider;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;


/**
 * Provides custom tab titles for generated in-memory Spoofax files,
 * such as the parsed AST of a file.
 */
public final class SpoofaxEditorTabTitleProvider implements EditorTabTitleProvider, FileIconProvider {

    @Nullable
    @Override
    public String getEditorTabTitle(@NotNull Project project, @NotNull VirtualFile file) {
        if (project == null || file.isDirectory()) return null;
        if (file instanceof LightVirtualFile) {

//            file.getName()
//            if (name.equals(file.getPresentableName())) {
//                return UniqueVFilePathBuilder.getInstance().getUniqueVirtualFilePath(project, file);
//            }
//            return name;
            // TODO: Use the below code to generate a unique name
        }
        return null;
    }


    public String getEditorTabTitle2(@NotNull Project project, @NotNull VirtualFile file) {
        UISettings uiSettings = UISettings.getInstanceOrNull();
        if (uiSettings == null || !uiSettings.getShowDirectoryForNonUniqueFilenames() || DumbService.isDumb(project)) {
            return null;
        }

        // Even though this is a 'tab title provider' it is used also when tabs are not shown, namely for building IDE frame title.
        String uniqueName = uiSettings.getEditorTabPlacement() == UISettings.TABS_NONE ?
                UniqueVFilePathBuilder.getInstance().getUniqueVirtualFilePath(project, file) :
                UniqueVFilePathBuilder.getInstance().getUniqueVirtualFilePathWithinOpenedFileEditors(project, file);
        uniqueName = getEditorTabText(uniqueName, File.separator, uiSettings.getHideKnownExtensionInTabs());
        return uniqueName.equals(file.getName()) ? null : uniqueName;
    }

    public static String getEditorTabText(String result, String separator, boolean hideKnownExtensionInTabs) {
        if (hideKnownExtensionInTabs) {
            String withoutExtension = FileUtilRt.getNameWithoutExtension(result);
            if (StringUtil.isNotEmpty(withoutExtension) && !withoutExtension.endsWith(separator)) {
                return withoutExtension;
            }
        }
        return result;
    }

    @Nullable
    @Override
    public String getEditorTabTooltipText(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        // TODO: Provide the full name
        return null;
    }

    @Nullable
    @Override
    public Icon getIcon(@NotNull VirtualFile file, int flags, @Nullable Project project) {
        // TODO: Provide the filetype icon with a 'generated' icon overlay (small generator icon in the corner, just like Generated Sources folder icon in IntelliJ).
        // See: intellij-community/platform/icons/src/modules/generatedFolder.svg
        // See: Icon generatedFolder = AllIcons.Modules.GeneratedFolder;
        if (project == null || file.isDirectory()) return null;
        return null;
    }

}
