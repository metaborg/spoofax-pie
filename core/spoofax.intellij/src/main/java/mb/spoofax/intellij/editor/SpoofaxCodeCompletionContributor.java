package mb.spoofax.intellij.editor;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.ui.LayeredIcon;
import com.intellij.ui.RowIcon;
import com.intellij.util.PlatformIcons;
import com.intellij.util.SmartList;
import com.intellij.util.ui.EmptyIcon;
import mb.common.codecompletion.CodeCompletionItem;
import mb.common.codecompletion.CodeCompletionResult;
import mb.common.editing.TextEdit;
import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.style.StyleNames;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Task;
import mb.pie.dagger.PieComponent;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.intellij.IntellijLanguageComponent;
import mb.spoofax.intellij.SpoofaxPlugin;
import mb.spoofax.intellij.resource.IntellijResourceRegistry;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Provider;
import javax.swing.*;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static mb.common.style.StyleNameConstants.*;

/**
 * Completion contributor for IntelliJ.
 */
public abstract class SpoofaxCodeCompletionContributor extends CompletionContributor {


    private final IntellijResourceRegistry resourceRegistry;
    private final LanguageInstance languageInstance;
    private final Provider<MixedSession> pieSessionProvider;

    protected SpoofaxCodeCompletionContributor(IntellijLanguageComponent languageComponent, PieComponent pieComponent) {
        this.resourceRegistry = SpoofaxPlugin.getResourceServiceComponent().getResourceRegistry();
        this.languageInstance = languageComponent.getLanguageInstance();
        this.pieSessionProvider = () -> pieComponent.getPie().newSession();

    }

    @Override
    public void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result) {
        final Resource file = resourceRegistry.getResource(parameters.getOriginalFile());
        final ResourceKey fileKey = file.getKey();
        final @Nullable ResourcePath projectRoot = null;// TODO: Get the project root
        final Region selection = Region.atOffset(parameters.getOffset());
        final CodeCompletionResult codeCompletionResult;
        try(final MixedSession session = this.pieSessionProvider.get()) {
            Task<Option<CodeCompletionResult>> codeCompletionTask = this.languageInstance.createCodeCompletionTask(selection, fileKey, projectRoot);
            final Option<CodeCompletionResult> maybeCodeCompletionResult = session.require(codeCompletionTask);
            if (maybeCodeCompletionResult.isNone()) return;  // No completions.
            codeCompletionResult = maybeCodeCompletionResult.unwrap();
        } catch(ExecException e) {
            throw new RuntimeException("Code completion on resource '" + fileKey + "' failed unexpectedly.", e);
        } catch(InterruptedException e) {
            return;
        }

        List<LookupElement> elements = IntStream.range(0, codeCompletionResult.getProposals().size()).mapToObj(i -> proposalToElement(codeCompletionResult.getProposals().get(i), i)).collect(Collectors.toList());
        result.addAllElements(elements);
    }

    private LookupElement proposalToElement(CodeCompletionItem proposal, int priority) {
        LookupElementBuilder element = LookupElementBuilder
            .create(proposal.getLabel())
            .withTailText(proposal.getParameters() + (proposal.getLocation().isEmpty() ? "" : " " + proposal.getLocation()))
            .withTypeText(proposal.getType().isEmpty() ? proposal.getDescription() : proposal.getType(), true)
            .withInsertHandler((ctx, item) -> insertHandler(ctx, item, proposal))
            .withIcon(getIcon(StyleNames.of(proposal.getKind())));
        return PrioritizedLookupElement.withPriority(element, priority);
    }

    private @Nullable Icon getIcon(StyleNames styles) {
        final @Nullable Icon kindIcon = getKindIcon(styles);
        final @Nullable Icon visibilityIcon = getVisibilityIcon(styles);
        final @Nullable Icon baseIcon = getBaseIcon(kindIcon, styles);
        if(baseIcon == null && visibilityIcon == null) return null;
        if(baseIcon == null) return EmptyIcon.create(PlatformIcons.CLASS_ICON /* only size is used */);
        if(visibilityIcon == null) return baseIcon;

        RowIcon resultIcon = new RowIcon(2);
        resultIcon.setIcon(baseIcon, 0);
        resultIcon.setIcon(visibilityIcon, 1);
        return resultIcon;
    }

    private static final List<Map.Entry<String, Function<StyleNames, @Nullable Icon>>> kindIcons = Arrays.asList(
        // @formatter:off
        new AbstractMap.SimpleImmutableEntry<>(ENTITY_ANNOTATION,   s -> PlatformIcons.ANNOTATION_TYPE_ICON),
        new AbstractMap.SimpleImmutableEntry<>(ENTITY_CLASS,        s -> s.anyStartsWith("meta.abstract") ? PlatformIcons.ABSTRACT_CLASS_ICON : PlatformIcons.CLASS_ICON),
        new AbstractMap.SimpleImmutableEntry<>(ENTITY_ENUM,         s -> PlatformIcons.ENUM_ICON),
        new AbstractMap.SimpleImmutableEntry<>(ENTITY_EXCEPTION,    s -> PlatformIcons.EXCEPTION_CLASS_ICON),
        new AbstractMap.SimpleImmutableEntry<>(ENTITY_FIELD,        s -> PlatformIcons.FIELD_ICON),
        new AbstractMap.SimpleImmutableEntry<>(ENTITY_FUNCTION,     s -> PlatformIcons.FUNCTION_ICON),
        new AbstractMap.SimpleImmutableEntry<>(ENTITY_IMPL,         s -> null),   // TODO: Find an icon
        new AbstractMap.SimpleImmutableEntry<>(ENTITY_INTERFACE,    s -> PlatformIcons.INTERFACE_ICON),
        new AbstractMap.SimpleImmutableEntry<>(ENTITY_METHOD,       s -> s.anyStartsWith("meta.abstract") ? PlatformIcons.ABSTRACT_METHOD_ICON : PlatformIcons.METHOD_ICON),
        new AbstractMap.SimpleImmutableEntry<>(ENTITY_NAMESPACE,    s -> PlatformIcons.PACKAGE_ICON),
        new AbstractMap.SimpleImmutableEntry<>(ENTITY_PACKAGE,      s -> PlatformIcons.PACKAGE_ICON),
        new AbstractMap.SimpleImmutableEntry<>(ENTITY_PREPROCESSOR, s -> null),   // TODO: Find an icon
        new AbstractMap.SimpleImmutableEntry<>(ENTITY_PROPERTY,     s -> PlatformIcons.PROPERTY_ICON),
        new AbstractMap.SimpleImmutableEntry<>(ENTITY_TRAIT,        s -> null),   // TODO: Find an icon
        new AbstractMap.SimpleImmutableEntry<>(ENTITY_TYPE,         s -> null),   // TODO: Find an icon
        new AbstractMap.SimpleImmutableEntry<>(ENTITY_VARIABLE,     s -> PlatformIcons.VARIABLE_ICON),
        new AbstractMap.SimpleImmutableEntry<>(ENTITY_UNION,        s -> null)    // TODO: Find an icon
        // @formatter:on
    );

    private @Nullable Icon getKindIcon(StyleNames styles) {
        for(Map.Entry<String, Function<StyleNames, @Nullable Icon>> entry : kindIcons) {
            if(styles.anyStartsWith(entry.getKey())) {
                return entry.getValue().apply(styles);
            }
        }
        return null;
    }

    private static final List<Map.Entry<String, Function<StyleNames, @Nullable Icon>>> visibilityIcons = Arrays.asList(
        // @formatter:off
        new AbstractMap.SimpleImmutableEntry<>(VISIBILITY_PUBLIC,     s -> PlatformIcons.PUBLIC_ICON),
        new AbstractMap.SimpleImmutableEntry<>(VISIBILITY_PACKAGE,    s -> PlatformIcons.PACKAGE_LOCAL_ICON),
        new AbstractMap.SimpleImmutableEntry<>(VISIBILITY_INTERNAL,   s -> PlatformIcons.PACKAGE_LOCAL_ICON),
        new AbstractMap.SimpleImmutableEntry<>(VISIBILITY_PROTECTED,  s -> PlatformIcons.PROTECTED_ICON),
        new AbstractMap.SimpleImmutableEntry<>(VISIBILITY_PRIVATE,    s -> PlatformIcons.PRIVATE_ICON)
        // @formatter:on
    );

    private @Nullable Icon getVisibilityIcon(StyleNames styles) {
        for(Map.Entry<String, Function<StyleNames, @Nullable Icon>> entry : visibilityIcons) {
            if(styles.anyStartsWith(entry.getKey())) {
                return entry.getValue().apply(styles);
            }
        }
        return null;
    }

    private @Nullable Icon getBaseIcon(@Nullable Icon kindIcon, StyleNames styles) {
        if(kindIcon == null) return null;

        SmartList<Icon> iconLayers = new SmartList<>();

        if(styles.anyStartsWith(STORAGE_EXTERNAL)) {
            iconLayers.add(PlatformIcons.LOCKED_ICON);
        }
        if(styles.anyStartsWith(STORAGE_STATIC)) {
            iconLayers.add(AllIcons.Nodes.StaticMark);
        }
        if(styles.anyStartsWith(ENTITY_EXCLUDED)) {
            iconLayers.add(PlatformIcons.EXCLUDED_FROM_COMPILE_ICON);
        }
        if(styles.anyStartsWith(ENTITY_TEST)) {
            // Currently has no icon.
        }

        if(!iconLayers.isEmpty()) {
            LayeredIcon layeredIcon = new LayeredIcon(1 + iconLayers.size());
            layeredIcon.setIcon(kindIcon, 0);
            for(int i = 0; i < iconLayers.size(); i++) {
                layeredIcon.setIcon(iconLayers.get(i), i + 1);
            }
            return layeredIcon;
        } else {
            return kindIcon;
        }
    }

    private void insertHandler(InsertionContext context, LookupElement item, CodeCompletionItem proposal) {
        final Editor editor = context.getEditor();
        final Document document = editor.getDocument();
        final int startOffset = context.getStartOffset();
        final int tailOffset = context.getTailOffset();
        // First, we replace the automatically inserted text with an empty string
        document.replaceString(startOffset, tailOffset, "");
        // Then we apply our text edits, which includes edits that replace the placeholder, if any
        // Note that we apply them back to front, such that an earlier text editor will not influence
        // the offset of the later text edits
        for (int i = (proposal.getEdits().size() - 1); i >= 0; i--) {
            final TextEdit edit = proposal.getEdits().get(i);
            document.replaceString(edit.getRegion().getStartOffset(), edit.getRegion().getEndOffset(), edit.getNewText());
        }
        assert Boolean.TRUE;
    }

}
