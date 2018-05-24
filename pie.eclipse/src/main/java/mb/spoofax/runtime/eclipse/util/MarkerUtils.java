package mb.spoofax.runtime.eclipse.util;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import mb.spoofax.api.message.Msg;
import mb.spoofax.api.message.MsgSeverityVisitor;
import mb.spoofax.api.region.Region;
import mb.spoofax.runtime.eclipse.SpoofaxPlugin;

/**
 * Utility functions for creating and removing {@link IMarker} instances.
 */
public final class MarkerUtils {
    private static final String id = SpoofaxPlugin.id + ".marker";
    private static final String infoPostfix = ".info";
    private static final String warningPostfix = ".warning";
    private static final String errorPostfix = ".error";


    public static IMarker createMarker(IResource resource, Msg msg) throws CoreException {
        final String markerId = id(msg);
        final IMarker marker = resource.createMarker(markerId);
        final Region region = msg.region();
        if(region != null) {
            marker.setAttribute(IMarker.CHAR_START, region.startOffset());
            // CHAR_END is exclusive, while region is inclusive: add 1
            marker.setAttribute(IMarker.CHAR_END, region.endOffset() + 1);
            // TODO: should line number be set? seems to work without it
            // marker.setAttribute(IMarker.LINE_NUMBER, region.startRow() + 1);
        } else {
            marker.setAttribute(IMarker.LINE_NUMBER, 1);
        }
        marker.setAttribute(IMarker.MESSAGE, msg.text());
        marker.setAttribute(IMarker.SEVERITY, severity(msg));
        marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
        return marker;
    }

    public static void clearAll(IResource resource) throws CoreException {
        resource.deleteMarkers(id, true, IResource.DEPTH_ZERO);
    }

    public static void clearAllRec(IResource resource) throws CoreException {
        resource.deleteMarkers(id, true, IResource.DEPTH_INFINITE);
    }


    public static int severity(Msg msg) {
        return msg.severity().accept(new MsgSeverityVisitor<Integer>() {
            @Override public Integer info(Msg message) {
                return IMarker.SEVERITY_INFO;
            }

            @Override public Integer warning(Msg message) {
                return IMarker.SEVERITY_WARNING;
            }

            @Override public Integer error(Msg message) {
                return IMarker.SEVERITY_ERROR;
            }
        }, msg);
    }

    public static String id(Msg msg) {
        return msg.severity().accept(new MsgSeverityVisitor<String>() {
            @Override public String info(Msg message) {
                return id + infoPostfix;
            }

            @Override public String warning(Msg message) {
                return id + warningPostfix;
            }

            @Override public String error(Msg message) {
                return id + errorPostfix;
            }
        }, msg);
    }
}
