package mb.spoofax.generator.util;

public class Conversion {
    public static String packageIdToPath(String packageId) {
        return packageId.replace('.', '/');
    }

    public static String pathToPackageId(String path) {
        return path.replace('/', '.');
    }
}
