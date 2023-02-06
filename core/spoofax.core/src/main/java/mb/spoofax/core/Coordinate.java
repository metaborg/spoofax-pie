package mb.spoofax.core;

import mb.common.option.Option;
import mb.common.util.StringUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class Coordinate implements Serializable {
    public final String groupId;
    public final String artifactId;
    public final Version version;

    public Coordinate(String groupId, String artifactId, Version version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public static Option<Coordinate> parse(String requirement) {
        if(StringUtil.isBlank(requirement)) {
            return Option.ofNone();
        }
        final int firstColonIndex = requirement.indexOf(':');
        if(firstColonIndex < 0) {
            return Option.ofNone();
        }
        final String groupId = requirement.substring(0, firstColonIndex);
        final int lastColonIndex = requirement.lastIndexOf(':');
        if(lastColonIndex == firstColonIndex) { // Only one ':', so this cannot be a valid coordinate.
            return Option.ofNone();
        }
        final String artifactId = requirement.substring(firstColonIndex + 1, lastColonIndex);
        final String versionString = requirement.substring(lastColonIndex + 1);
        if(StringUtil.isBlank(versionString)) {
            return Option.ofNone();
        }
        final Version version = Version.parse(versionString);
        return Option.ofSome(new Coordinate(groupId, artifactId, version));
    }


    public CoordinateRequirement toCoordinateRequirement() {
        return new CoordinateRequirement(groupId, artifactId, version);
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Coordinate that = (Coordinate)o;
        if(!groupId.equals(that.groupId)) return false;
        if(!artifactId.equals(that.artifactId)) return false;
        return version.equals(that.version);
    }

    @Override public int hashCode() {
        int result = groupId.hashCode();
        result = 31 * result + artifactId.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }

    @Override public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }
}
