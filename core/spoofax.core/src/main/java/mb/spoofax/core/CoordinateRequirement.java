package mb.spoofax.core;

import mb.common.option.Option;
import mb.common.util.StringUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class CoordinateRequirement implements Serializable {
    public final String groupId;
    public final String artifactId;
    public final @Nullable Version versionRequirement;

    public CoordinateRequirement(String groupId, String artifactId, @Nullable Version versionRequirement) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.versionRequirement = versionRequirement;
    }

    public CoordinateRequirement(String groupId, String artifactId) {
        this(groupId, artifactId, null);
    }

    public CoordinateRequirement(Coordinate coordinate) {
        this(coordinate.groupId, coordinate.artifactId, coordinate.version);
    }

    public static Option<CoordinateRequirement> parse(String requirement) {
        if(StringUtil.isBlank(requirement)) {
            return Option.ofNone();
        }
        final int firstColonIndex = requirement.indexOf(':');
        if(firstColonIndex < 0) {
            return Option.ofNone();
        }
        final String groupId = requirement.substring(0, firstColonIndex);
        final int lastColonIndex = requirement.lastIndexOf(':');
        final String artifactId;
        final @Nullable String versionString;
        if(lastColonIndex == firstColonIndex) { // Only one ':', so id is until the end of the string.
            artifactId = requirement.substring(firstColonIndex + 1);
            versionString = null;
        } else { // Id starts after first colon, until the last colon.
            artifactId = requirement.substring(firstColonIndex + 1, lastColonIndex);
            versionString = requirement.substring(lastColonIndex + 1);
        }
        final @Nullable Version versionRequirement;
        if(StringUtil.isBlank(versionString) || versionString == null /* extra check for static semantics */) {
            versionRequirement = null;
        } else {
            versionRequirement = Version.parse(versionString);
        }
        return Option.ofSome(new CoordinateRequirement(groupId, artifactId, versionRequirement));
    }


    public boolean matches(Coordinate coordinate) {
        if(!groupId.equals(coordinate.groupId)) return false;
        if(!artifactId.equals(coordinate.artifactId)) return false;
        return versionRequirement == null || versionRequirement.equals(coordinate.version);
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final CoordinateRequirement that = (CoordinateRequirement)o;
        if(!groupId.equals(that.groupId)) return false;
        if(!artifactId.equals(that.artifactId)) return false;
        return versionRequirement != null ? versionRequirement.equals(that.versionRequirement) : that.versionRequirement == null;
    }

    @Override public int hashCode() {
        int result = groupId.hashCode();
        result = 31 * result + artifactId.hashCode();
        result = 31 * result + (versionRequirement != null ? versionRequirement.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return groupId + ":" + artifactId + (versionRequirement != null ? (":" + versionRequirement) : "");
    }
}
