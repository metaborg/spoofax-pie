package mb.spoofax.core.platform;

import mb.fs.api.node.FSNode;
import mb.fs.api.path.FSPath;

public interface ResourceService {
    FSNode getNode(FSPath path);
}
