package mb.spoofax.core;

import mb.common.util.StringUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Representation for the version of a component, following the Maven/Gradle versioning style.
 */
public class Version implements Comparable<Version>, Serializable {
    private static final String snapshot = "snapshot";
    private static final Comparator<Integer> integerComparator = Comparator.nullsLast(Comparator.naturalOrder());
    @SuppressWarnings("ConstantConditions") private static final Comparator<Version> comparator = Comparator
        .<Version, Integer>comparing(v -> v.major, integerComparator)
        .thenComparing(v -> v.minor, integerComparator)
        .thenComparing(v -> v.incremental, integerComparator)
        .thenComparing(v -> v.qualifier, (left, right) -> {
            final boolean leftNull = StringUtil.isBlank(left);
            final boolean rightNull = StringUtil.isBlank(right);
            if(leftNull && rightNull) {
                return 0;
            }

            final boolean leftSnapshot = !leftNull && left.toLowerCase().contains(snapshot);
            final boolean rightSnapshot = !rightNull && right.toLowerCase().contains(snapshot);
            if(leftSnapshot && rightSnapshot) {
                return 0;
            }

            if(leftNull) {
                return rightSnapshot ? -1 : 1;
            } else if(rightNull) {
                return leftSnapshot ? 1 : -1;
            } else {
                if(leftSnapshot) {
                    return 1;
                } else if(rightSnapshot) {
                    return -1;
                } else {
                    return left.compareTo(right);
                }
            }
        });
    private static final Pattern decimalsPattern = Pattern.compile("\\d+");

    protected final @Nullable Integer major;
    protected final @Nullable Integer minor;
    protected final @Nullable Integer incremental;
    protected final @Nullable String qualifier;


    public Version(int major) {
        this(major, null, null, null);
    }

    public Version(int major, int minor) {
        this(major, minor, null, null);
    }

    public Version(int major, int minor, int incremental) {
        this(major, minor, incremental, null);
    }

    public Version(@Nullable Integer major, @Nullable Integer minor, @Nullable Integer incremental, @Nullable String qualifier) {
        this.major = major;
        this.minor = minor;
        this.incremental = incremental;
        this.qualifier = qualifier;
    }

    public static Version parse(String version) {
        // Copied and edited from: https://github.com/apache/maven/blob/master/maven-artifact/src/main/java/org/apache/maven/artifact/versioning/DefaultArtifactVersion.java
        @Nullable Integer major = null;
        @Nullable Integer minor = null;
        @Nullable Integer incremental = null;
        @Nullable String qualifier = null;

        final String numericalPart;
        final int hyphenIndex = version.indexOf('-');
        if(hyphenIndex < 0) {
            numericalPart = version;
        } else {
            numericalPart = version.substring(0, hyphenIndex);
            // Qualifier is part of version starting from (including) "-".
            qualifier = version.substring(hyphenIndex);
        }

        if((!numericalPart.contains(".")) && !numericalPart.startsWith("0")) {
            try {
                major = Integer.valueOf(numericalPart);
            } catch(NumberFormatException e) {
                // Qualifier is the whole version, including "-".
                qualifier = version;
            }
        } else {
            boolean fallback = false;
            final StringTokenizer tok = new StringTokenizer(numericalPart, ".");
            try {
                major = getNextIntegerToken(tok);
                if(tok.hasMoreTokens()) {
                    minor = getNextIntegerToken(tok);
                }
                if(tok.hasMoreTokens()) {
                    incremental = getNextIntegerToken(tok);
                }
                if(tok.hasMoreTokens()) {
                    qualifier = tok.nextToken();
                    fallback = decimalsPattern.matcher(qualifier).matches();
                }
                // String tokenizer won't detect these and ignores them.
                if(numericalPart.contains("..") || numericalPart.startsWith(".") || numericalPart.endsWith(".")) {
                    fallback = true;
                }
            } catch(NumberFormatException e) {
                fallback = true;
            }

            if(fallback) {
                // Qualifier is the whole version, including "-".
                qualifier = version;
                major = null;
                minor = null;
                incremental = null;
            }
        }

        return new Version(major, minor, incremental, qualifier);
    }

    private static Integer getNextIntegerToken(StringTokenizer tok) {
        // Copied from: https://github.com/apache/maven/blob/master/maven-artifact/src/main/java/org/apache/maven/artifact/versioning/DefaultArtifactVersion.java
        try {
            final String s = tok.nextToken();
            if((s.length() > 1) && s.startsWith("0")) {
                throw new NumberFormatException("Number part has a leading 0: '" + s + "'");
            }
            return Integer.valueOf(s);
        } catch(NoSuchElementException e) {
            throw new NumberFormatException("Number is invalid");
        }
    }


    @Override public int compareTo(Version other) {
        return comparator.compare(this, other);
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Version that = (Version)o;
        if(major != null ? !major.equals(that.major) : that.major != null) return false;
        if(minor != null ? !minor.equals(that.minor) : that.minor != null) return false;
        if(incremental != null ? !incremental.equals(that.incremental) : that.incremental != null) return false;
        return qualifier != null ? qualifier.equals(that.qualifier) : that.qualifier == null;
    }

    @Override public int hashCode() {
        int result = major != null ? major.hashCode() : 0;
        result = 31 * result + (minor != null ? minor.hashCode() : 0);
        result = 31 * result + (incremental != null ? incremental.hashCode() : 0);
        result = 31 * result + (qualifier != null ? qualifier.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();
        if(major != null) {
            sb.append(major);
        } else if(minor != null || incremental != null) {
            sb.append(0);
        }

        if(minor != null) {
            sb.append('.');
            sb.append(minor);
        } else if(incremental != null) {
            sb.append(0);
        }

        if(incremental != null) {
            sb.append('.');
            sb.append(incremental);
        }

        if(qualifier != null) {
            sb.append(qualifier);
        }

        return sb.toString();
    }
}
