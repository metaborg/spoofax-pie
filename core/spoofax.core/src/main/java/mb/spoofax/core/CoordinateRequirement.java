package mb.spoofax.core;

import mb.common.option.Option;
import mb.common.util.StringUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class CoordinateRequirement implements Serializable {
    public final @Nullable String groupRequirement;
    public final String id;
    public final @Nullable Version versionRequirement;

    public CoordinateRequirement(@Nullable String groupRequirement, String id, @Nullable Version versionRequirement) {
        this.groupRequirement = groupRequirement;
        this.id = id;
        this.versionRequirement = versionRequirement;
    }

    public CoordinateRequirement(@Nullable String groupRequirement, String id) {
        this(groupRequirement, id, null);
    }

    public CoordinateRequirement(String id, @Nullable Version versionRequirement) {
        this(null, id, versionRequirement);
    }

    public CoordinateRequirement(String id) {
        this(null, id, null);
    }

    public CoordinateRequirement(Coordinate coordinate) {
        this(coordinate.group, coordinate.id, coordinate.version);
    }

    public static Option<CoordinateRequirement> parse(String requirement) {
        if(StringUtil.isBlank(requirement)) {
            return Option.ofNone();
        }
        final int firstColonIndex = requirement.indexOf(':');
        if(firstColonIndex < 0) {
            return Option.ofSome(new CoordinateRequirement(requirement));
        }
        final String group = requirement.substring(0, firstColonIndex);
        final int lastColonIndex = requirement.lastIndexOf(':');
        final String id;
        final @Nullable String versionString;
        if(lastColonIndex == firstColonIndex) { // Only one ':', so id is until the end of the string.
            id = requirement.substring(firstColonIndex + 1);
            versionString = null;
        } else { // Id starts after first colon, until the last colon.
            id = requirement.substring(firstColonIndex + 1, lastColonIndex);
            versionString = requirement.substring(lastColonIndex + 1);
        }
        final @Nullable Version versionRequirement;
        if(StringUtil.isBlank(versionString) || versionString == null /* extra check for static semantics */) {
            versionRequirement = null;
        } else {
            versionRequirement = Version.parse(versionString);
        }
        return Option.ofSome(new CoordinateRequirement(group, id, versionRequirement));
    }


    public boolean matches(Coordinate coordinate) {
        if(groupRequirement != null && !groupRequirement.equals(coordinate.group)) return false;
        if(!id.equals(coordinate.id)) return false;
        if(versionRequirement != null && !versionRequirement.equals(coordinate.version)) return false;
        return true;
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final CoordinateRequirement that = (CoordinateRequirement)o;
        if(groupRequirement != null ? !groupRequirement.equals(that.groupRequirement) : that.groupRequirement != null)
            return false;
        if(!id.equals(that.id)) return false;
        return versionRequirement != null ? versionRequirement.equals(that.versionRequirement) : that.versionRequirement == null;
    }

    @Override public int hashCode() {
        int result = groupRequirement != null ? groupRequirement.hashCode() : 0;
        result = 31 * result + id.hashCode();
        result = 31 * result + (versionRequirement != null ? versionRequirement.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return (groupRequirement != null ? (groupRequirement + ":") : "") + id + (versionRequirement != null ? (":" + versionRequirement) : "");
    }
}
