package mb.spoofax.lwb.eclipse.compiler;

import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.eclipse.EclipseParticipant;
import mb.spoofax.eclipse.EclipsePlatformComponent;
import mb.spoofax.eclipse.EclipseResourceServiceComponent;
import mb.spoofax.eclipse.log.EclipseLoggerComponent;
import mb.spoofax.lwb.compiler.SpoofaxLwbCompilerJavaModule;
import mb.spoofax.lwb.compiler.SpoofaxLwbCompilerModule;
import mb.spoofax.lwb.compiler.SpoofaxLwbCompilerParticipant;

import java.nio.charset.StandardCharsets;

public class SpoofaxLwbCompilerEclipseParticipant extends SpoofaxLwbCompilerParticipant<EclipseLoggerComponent, EclipseResourceServiceComponent, EclipsePlatformComponent> implements EclipseParticipant {
    public SpoofaxLwbCompilerEclipseParticipant() {
        super(createModule(), new SpoofaxLwbCompilerJavaModule());
    }

    private static SpoofaxLwbCompilerModule createModule() {
        final SpoofaxLwbCompilerModule module = new SpoofaxLwbCompilerModule(new TemplateCompiler(StandardCharsets.UTF_8));
        module.setParticipantClassQualifiedIdSelector(input -> input.eclipseProjectInput()
            // Use Eclipse participant factory to initialize their singletons and to ensure an Eclipse language component is created.
            .map(e -> e.participantFactory().qualifiedId())
            // Fallback to default if Eclipse project input is somehow unavailable.
            .orElse(input.adapterProjectInput().participant().qualifiedId())
        );
        return module;
    }
}
