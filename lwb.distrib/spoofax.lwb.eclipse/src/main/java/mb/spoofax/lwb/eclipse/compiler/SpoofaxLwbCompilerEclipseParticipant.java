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
        super(new SpoofaxLwbCompilerModule(new TemplateCompiler(StandardCharsets.UTF_8)), new SpoofaxLwbCompilerJavaModule());
    }
}
