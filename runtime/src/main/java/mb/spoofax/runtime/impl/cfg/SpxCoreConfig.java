package mb.spoofax.runtime.impl.cfg;

import mb.pie.vfs.path.PPath;
import org.immutables.serial.Serial;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.List;

@Value.Immutable
@Serial.Version(value = 1L)
public interface SpxCoreConfig extends Serializable {
    @Value.Parameter PPath dir();

    @Value.Parameter boolean isLangSpec();

    @Value.Parameter List<String> extensions();
}
