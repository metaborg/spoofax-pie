package mb.spoofax.intellij;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

public class IntellijFileTypeFactory extends FileTypeFactory {
    private final IntellijLanguageComponent languageComponent;

    public IntellijFileTypeFactory(IntellijLanguageComponent languageComponent) {
        this.languageComponent = languageComponent;
    }

    @Override public void createFileTypes(@NotNull FileTypeConsumer consumer) {
        final IntellijLanguageFileType fileType = languageComponent.getFileType();
        consumer.consume(fileType, fileType.getDefaultExtension());
    }
}
