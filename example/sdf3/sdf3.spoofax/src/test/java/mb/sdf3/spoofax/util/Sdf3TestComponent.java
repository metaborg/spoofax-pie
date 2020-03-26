package mb.sdf3.spoofax.util;

import dagger.Component;
import mb.sdf3.spoofax.Sdf3Component;
import mb.sdf3.spoofax.Sdf3Module;
import mb.sdf3.spoofax.task.Sdf3Parse;
import mb.sdf3.spoofax.task.Sdf3ToCompletionColorer;
import mb.sdf3.spoofax.task.Sdf3ToCompletion;
import mb.sdf3.spoofax.task.Sdf3ToCompletionRuntime;
import mb.sdf3.spoofax.task.Sdf3ToDynsemSignature;
import mb.sdf3.spoofax.task.Sdf3ToNormalForm;
import mb.sdf3.spoofax.task.Sdf3ToPermissive;
import mb.sdf3.spoofax.task.Sdf3ToPrettyPrinter;
import mb.sdf3.spoofax.task.Sdf3ToSignature;
import mb.sdf3.spoofax.task.Sdf3ToTable;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.platform.PlatformComponent;

@LanguageScope @Component(modules = {Sdf3Module.class}, dependencies = PlatformComponent.class)
public interface Sdf3TestComponent extends Sdf3Component {
    Sdf3Parse getParse();

    Sdf3ToCompletionColorer getToCompletionColorer();

    Sdf3ToCompletionRuntime getToCompletionRuntime();

    Sdf3ToCompletion getToCompletion();

    Sdf3ToSignature getToSignature();

    Sdf3ToDynsemSignature getToDynsemSignature();

    Sdf3ToPrettyPrinter getToPrettyPrinter();

    Sdf3ToPermissive getToPermissive();

    Sdf3ToNormalForm getToNormalForm();

    Sdf3ToTable getToTable();
}
