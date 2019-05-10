package mb.tiger.intellij;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;

public class TigerFileTypeFactory extends FileTypeFactory {
    @Override public void createFileTypes(FileTypeConsumer consumer) {
        consumer.consume(new TigerFileType());
    }
}
