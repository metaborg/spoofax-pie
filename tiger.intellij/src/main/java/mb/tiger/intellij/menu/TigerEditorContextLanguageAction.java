package mb.tiger.intellij.menu;

import mb.spoofax.core.language.command.CommandRequest;
import mb.spoofax.intellij.IntellijLanguageComponent;
import mb.spoofax.intellij.SpoofaxPlugin;
import mb.spoofax.intellij.menu.EditorContextLanguageAction;
import mb.spoofax.intellij.pie.PieRunner;
import mb.spoofax.intellij.resource.IntellijResourceRegistry;
import mb.tiger.intellij.TigerPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import javax.swing.*;


public final class TigerEditorContextLanguageAction extends EditorContextLanguageAction {

    /**
     * Factory class for the {@link EditorContextLanguageAction} class.
     */
    public static final class Factory implements EditorContextLanguageAction.Factory {

        private final IntellijResourceRegistry resourceRegistry;
        private final PieRunner pieRunner;

        @Inject
        public Factory(IntellijResourceRegistry resourceRegistry,
                       PieRunner pieRunner) {
            this.resourceRegistry = resourceRegistry;
            this.pieRunner = pieRunner;
        }

        @Override
        public TigerEditorContextLanguageAction create(
                String id,
                CommandRequest commandRequest,
                @Nullable String text,
                @Nullable String description,
                @Nullable Icon icon) {
            return new TigerEditorContextLanguageAction(id, commandRequest, text, description, icon,
                    this.resourceRegistry, this.pieRunner);
        }

    }

    public TigerEditorContextLanguageAction(
            String id, CommandRequest commandRequest, @Nullable String text,
            @Nullable String description, @Nullable Icon icon, IntellijResourceRegistry resourceRegistry, PieRunner pieRunner) {
        super(id, commandRequest, text, description, icon, TigerPlugin.getComponent(), resourceRegistry, pieRunner);
    }

}
