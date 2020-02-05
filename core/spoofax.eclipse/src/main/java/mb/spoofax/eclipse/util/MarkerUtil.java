package mb.spoofax.eclipse.util;

import mb.common.message.Severity;
import mb.common.region.Region;
import mb.spoofax.eclipse.EclipseIdentifiers;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * Utility functions for creating and removing {@link IMarker} instances.
 */
public final class MarkerUtil {
    public static IMarker createMarker(
        EclipseIdentifiers eclipseIdentifiers,
        String text,
        Severity severity,
        IResource resource,
        @Nullable Region region
    ) throws CoreException {
        final int eclipseSeverity = severity(severity);
        final String markerId = id(eclipseIdentifiers, eclipseSeverity);
        final IMarker marker = resource.createMarker(markerId);
        if(region != null) {
            marker.setAttribute(IMarker.CHAR_START, region.getStartOffset());
            marker.setAttribute(IMarker.CHAR_END, region.getEndOffset());
        } else {
            marker.setAttribute(IMarker.LINE_NUMBER, 1);
        }
        marker.setAttribute(IMarker.MESSAGE, text);
        marker.setAttribute(IMarker.SEVERITY, eclipseSeverity);
        marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
        return marker;
    }

    public static void clearAll(EclipseIdentifiers eclipseIdentifiers, IResource resource) throws CoreException {
        resource.deleteMarkers(eclipseIdentifiers.getBaseMarker(), true, IResource.DEPTH_ZERO);
    }

    public static void clearAllRec(EclipseIdentifiers eclipseIdentifiers, IResource resource) throws CoreException {
        resource.deleteMarkers(eclipseIdentifiers.getBaseMarker(), true, IResource.DEPTH_INFINITE);
    }


    private static int severity(Severity severity) {
        switch(severity) {
            case Info:
                return IMarker.SEVERITY_INFO;
            case Warning:
                return IMarker.SEVERITY_WARNING;
            case Error:
                return IMarker.SEVERITY_ERROR;
            default:
                return IMarker.SEVERITY_INFO;
        }
    }

    private static String id(EclipseIdentifiers eclipseIdentifiers, int severity) {
        switch(severity) {
            case IMarker.SEVERITY_INFO:
                return eclipseIdentifiers.getInfoMarker();
            case IMarker.SEVERITY_WARNING:
                return eclipseIdentifiers.getWarningMarker();
            case IMarker.SEVERITY_ERROR:
                return eclipseIdentifiers.getErrorMarker();
            default:
                return eclipseIdentifiers.getBaseMarker();
        }
    }
}
