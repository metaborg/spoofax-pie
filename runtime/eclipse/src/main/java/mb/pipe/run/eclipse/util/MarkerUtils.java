package mb.pipe.run.eclipse.util;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import mb.pipe.run.core.model.message.IMsg;
import mb.pipe.run.core.model.message.MsgSeverityVisitor;
import mb.pipe.run.core.model.region.IRegion;
import mb.pipe.run.eclipse.PipePlugin;

/**
 * Utility functions for creating and removing {@link IMarker} instances.
 */
public final class MarkerUtils {
    private static final String id = PipePlugin.id + ".marker";
    private static final String infoPostfix = ".info";
    private static final String warningPostfix = ".warning";
    private static final String errorPostfix = ".error";


    public static IMarker createMarker(IResource resource, IMsg msg) throws CoreException {
        final String markerId = id(msg);
        final IMarker marker = resource.createMarker(markerId);
        final IRegion region = msg.region();
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


    public static int severity(IMsg msg) {
        return msg.severity().accept(new MsgSeverityVisitor<Integer>() {
            @Override public Integer info(IMsg message) {
                return IMarker.SEVERITY_INFO;
            }

            @Override public Integer warning(IMsg message) {
                return IMarker.SEVERITY_WARNING;
            }

            @Override public Integer error(IMsg message) {
                return IMarker.SEVERITY_ERROR;
            }
        }, msg);
    }

    public static String id(IMsg msg) {
        return msg.severity().accept(new MsgSeverityVisitor<String>() {
            @Override public String info(IMsg message) {
                return id + infoPostfix;
            }

            @Override public String warning(IMsg message) {
                return id + warningPostfix;
            }

            @Override public String error(IMsg message) {
                return id + errorPostfix;
            }
        }, msg);
    }
}
