package mb.spoofax.eclipse.editor;

import mb.common.message.KeyedMessages;
import mb.pie.api.SerializableConsumer;
import mb.resource.ResourceRuntimeException;
import mb.spoofax.eclipse.EclipseIdentifiers;
import mb.spoofax.eclipse.EclipsePlatformComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.pie.WorkspaceUpdate;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

public abstract class CheckCallback implements SerializableConsumer<KeyedMessages> {
    public interface Factory {
        CheckCallback create(EclipseResourcePath file);
    }

    private final EclipseResourcePath file;

    public CheckCallback(EclipseResourcePath file) {
        this.file = file;
    }

    @Override public void accept(KeyedMessages messages) {
        final EclipsePlatformComponent platformComponent = SpoofaxPlugin.getPlatformComponent();
        final WorkspaceUpdate callbackWorkspaceUpdate = platformComponent.getWorkspaceUpdateFactory().create(getEclipseIdentifiers());
        callbackWorkspaceUpdate.replaceMessages(messages, file);
        @Nullable ISchedulingRule rule;
        try {
            rule = platformComponent.getResourceUtil().getEclipseFile(file);
        } catch(ResourceRuntimeException e) {
            rule = null;
        }
        callbackWorkspaceUpdate.update(rule, null);
    }

    protected abstract EclipseIdentifiers getEclipseIdentifiers();
}
