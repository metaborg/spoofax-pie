package mb.spoofax.core.language.transform;

import mb.common.region.Region;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class FileRegionSubject extends FileSubject implements RegionSubject {
    private final Region region;


    public FileRegionSubject(ResourceKey resourceKey, Region region) {
        super(resourceKey);
        this.region = region;
    }


    public ResourceKey getFile() {
        return file;
    }

    public Region getRegion() {
        return region;
    }


    @Override public void accept(TransformSubjectVisitor visitor) {
        visitor.fileRegion(file, region);
    }


    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        if(!super.equals(obj)) return false;
        final FileRegionSubject other = (FileRegionSubject) obj;
        return region.equals(other.region);
    }

    @Override public int hashCode() {
        return Objects.hash(super.hashCode(), region);
    }

    @Override public String toString() {
        return file.toString() + "@" + region.toString();
    }
}
