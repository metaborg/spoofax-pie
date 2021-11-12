package mb.strategolib;

import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Copy of Stratego2LibInfo because this Java library cannot depend on `stratego.build` due to `strategolib.eclipse`
 * needing a dependency to `stratego.eclipse` (because that exports `stratego.build`), which would cause a cycle because
 * `stratego.eclipse` requires `strategolib.eclipse`.
 */
public class StrategoLibInfo implements Serializable {
    public final ResourcePath str2libFile;
    public final ArrayList<ResourcePath> jarFilesOrDirectories;

    public StrategoLibInfo(ResourcePath str2libFile, ArrayList<ResourcePath> jarFilesOrDirectories) {
        this.str2libFile = str2libFile;
        this.jarFilesOrDirectories = jarFilesOrDirectories;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final StrategoLibInfo that = (StrategoLibInfo)o;
        if(!str2libFile.equals(that.str2libFile)) return false;
        return jarFilesOrDirectories.equals(that.jarFilesOrDirectories);
    }

    @Override public int hashCode() {
        int result = str2libFile.hashCode();
        result = 31 * result + jarFilesOrDirectories.hashCode();
        return result;
    }

    @Override public String toString() {
        return "StrategoLibInfo{" +
            "str2libFile=" + str2libFile +
            ", jarFilesOrDirectories=" + jarFilesOrDirectories +
            '}';
    }
}
