package mb.spoofax.runtime.eclipse.util;

import mb.spoofax.api.message.Message;
import mb.spoofax.api.region.Region;
import mb.spoofax.runtime.eclipse.SpoofaxPlugin;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * Utility functions for creating and removing {@link IMarker} instances.
 */
public final class MarkerUtils {
    private static final String id = SpoofaxPlugin.id + ".marker";
    private static final String errorPostfix = ".error";
    private static final String warningPostfix = ".warning";
    private static final String infoPostfix = ".info";


    public static IMarker createMarker(IResource resource, Message msg) throws CoreException {
        final int severity = severity(msg);
        final String markerId = id(severity);
        final IMarker marker = resource.createMarker(markerId);
        final Region region = msg.region;
        if(region != null) {
            marker.setAttribute(IMarker.CHAR_START, region.startOffset);
            // CHAR_END is exclusive, while region is inclusive: add 1
            marker.setAttribute(IMarker.CHAR_END, region.endOffset + 1);
            // TODO: should line number be set? seems to work without it
            // marker.setAttribute(IMarker.LINE_NUMBER, region.startRow() + 1);
        } else {
            marker.setAttribute(IMarker.LINE_NUMBER, 1);
        }
        marker.setAttribute(IMarker.MESSAGE, msg.text);
        marker.setAttribute(IMarker.SEVERITY, severity);
        marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
        return marker;
    }

    public static void clearAll(IResource resource) throws CoreException {
        resource.deleteMarkers(id, true, IResource.DEPTH_ZERO);
    }

    public static void clearAllRec(IResource resource) throws CoreException {
        resource.deleteMarkers(id, true, IResource.DEPTH_INFINITE);
    }


    public static int severity(Message message) {
        switch(message.severity) {
            case Error:
                return IMarker.SEVERITY_ERROR;
            case Warn:
                return IMarker.SEVERITY_WARNING;
            case Info:
                return IMarker.SEVERITY_INFO;
            case Debug:
                return IMarker.SEVERITY_INFO;
            case Trace:
                return IMarker.SEVERITY_INFO;
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
            case IMarker.SEVERITY_INFO:
                return id + infoPostfix;
            default:
                return id + infoPostfix;
        }
    }
}
