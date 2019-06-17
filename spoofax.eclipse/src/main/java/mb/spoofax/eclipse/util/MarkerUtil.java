package mb.spoofax.eclipse.util;

import mb.common.message.Severity;
import mb.common.region.Region;
import mb.spoofax.eclipse.SpoofaxPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * Utility functions for creating and removing {@link IMarker} instances.
 */
public final class MarkerUtil {
    private static final String id = SpoofaxPlugin.id + ".marker";
    private static final String errorPostfix = ".error";
    private static final String warningPostfix = ".warning";
    private static final String infoPostfix = ".info";


    public static IMarker createMarker(String text, Severity severity, IResource resource, @Nullable Region region) throws CoreException {
        final int eclipseSeverity = severity(severity);
        final String markerId = id(eclipseSeverity);
        final IMarker marker = resource.createMarker(markerId);
        if(region != null) {
            marker.setAttribute(IMarker.CHAR_START, region.startOffset);
            // CHAR_END is exclusive, while region is inclusive: add 1
            marker.setAttribute(IMarker.CHAR_END, region.endOffset + 1);
        } else {
            marker.setAttribute(IMarker.LINE_NUMBER, 1);
        }
        marker.setAttribute(IMarker.MESSAGE, text);
        marker.setAttribute(IMarker.SEVERITY, eclipseSeverity);
        marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
        return marker;
    }

    public static void clearAll(IResource resource) throws CoreException {
        resource.deleteMarkers(id, true, IResource.DEPTH_ZERO);
    }

    public static void clearAllRec(IResource resource) throws CoreException {
        resource.deleteMarkers(id, true, IResource.DEPTH_INFINITE);
    }


    public static int severity(Severity severity) {
        switch(severity) {
            case Error:
                return IMarker.SEVERITY_ERROR;
            case Warning:
                return IMarker.SEVERITY_WARNING;
            default:
                return IMarker.SEVERITY_INFO;
        }
    }

    public static String id(int severity) {
        switch(severity) {
            case IMarker.SEVERITY_ERROR:
                return id + errorPostfix;
            case IMarker.SEVERITY_WARNING:
                return id + warningPostfix;
            default:
                return id + infoPostfix;
        }
    }
}
