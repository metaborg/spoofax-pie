package mb.spoofax.generator;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Optional;
import java.util.Properties;

@Value.Immutable
public interface SpoofaxCoreGeneratorInput extends Serializable {
    String groupId();

    String id();

    String packageId();

    default String packagePath() {
        return Conversion.packageIdToPath(packageId());
    }

    String name();


    @Value.Default default String classIdSuffix() {
        return name();
    }


    @Value.Default default String parseTableGeneratedClassId() {
        return classIdSuffix() + "ParseTable";
    }

    Optional<String> parseTableCustomClassId();

    @Value.Default default String parseTableResourceLocation() {
        return packagePath() + "/target/metaborg/sdf.tbl";
    }


    default Properties savePersistentProperties() {
        final Properties properties = new Properties();
        properties.setProperty("parseTableGeneratedClassId", parseTableGeneratedClassId());
        return properties;
    }

    static void loadPersistentProperties(Properties properties, ImmutableSpoofaxCoreGeneratorInput.Builder builder) {
        final @Nullable String parseTableGeneratedClassId = properties.getProperty("parseTableGeneratedClassId");
        if(parseTableGeneratedClassId != null) {
            builder.parseTableGeneratedClassId(parseTableGeneratedClassId);
        }
    }
}
