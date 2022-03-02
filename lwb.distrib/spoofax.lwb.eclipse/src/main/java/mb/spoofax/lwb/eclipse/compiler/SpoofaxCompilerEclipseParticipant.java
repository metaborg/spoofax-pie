package mb.spoofax.lwb.eclipse.compiler;

import mb.spoofax.compiler.dagger.SpoofaxCompilerModule;
import mb.spoofax.compiler.dagger.SpoofaxCompilerParticipant;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.eclipse.EclipseParticipant;
import mb.spoofax.eclipse.EclipsePlatformComponent;
import mb.spoofax.eclipse.EclipseResourceServiceComponent;
import mb.spoofax.eclipse.log.EclipseLoggerComponent;
import mb.spoofax.lwb.compiler.SpoofaxLwbCompilerJavaModule;
import mb.spoofax.lwb.compiler.SpoofaxLwbCompilerModule;
import mb.spoofax.lwb.compiler.SpoofaxLwbCompilerParticipant;

import java.nio.charset.StandardCharsets;

public class SpoofaxCompilerEclipseParticipant extends SpoofaxCompilerParticipant<EclipseLoggerComponent, EclipseResourceServiceComponent, EclipsePlatformComponent> implements EclipseParticipant {
    public SpoofaxCompilerEclipseParticipant() {
        super(new SpoofaxCompilerModule(new TemplateCompiler(StandardCharsets.UTF_8)));
    }
}
