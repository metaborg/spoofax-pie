package mb.spoofax.runtime.impl.cfg;

import java.io.Serializable;
import java.util.List;

import org.immutables.serial.Serial;
import org.immutables.value.Value;

import mb.vfs.path.PPath;

@Value.Immutable
@Serial.Version(value = 1L)
public interface SpxCoreConfig extends Serializable {
    @Value.Parameter PPath dir();

    @Value.Parameter boolean isLangSpec();

    @Value.Parameter List<String> extensions();
}
