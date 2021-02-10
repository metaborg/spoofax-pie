package mb.chars;

import mb.log.stream.StreamLoggerFactory;
import mb.pie.api.MixedSession;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.fs.FSPath;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.spoofax.core.platform.BaseResourceServiceComponent;
import mb.spoofax.core.platform.BaseResourceServiceModule;
import mb.spoofax.core.platform.DaggerBaseResourceServiceComponent;
import mb.spoofax.core.platform.DaggerPlatformComponent;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.PlatformPieModule;

import java.util.Optional;

public class Main {
    public static void main(String[] args) throws Exception {
        final CharsResourcesComponent resourcesComponent = DaggerCharsResourcesComponent.create();
        final BaseResourceServiceModule resourceServiceModule = new BaseResourceServiceModule()
            .addRegistriesFrom(resourcesComponent);
        final BaseResourceServiceComponent resourceServiceComponent = DaggerBaseResourceServiceComponent.builder()
            .baseResourceServiceModule(resourceServiceModule)
            .build();
        final PlatformComponent platformComponent = DaggerPlatformComponent.builder()
            .loggerFactoryModule(new LoggerFactoryModule(StreamLoggerFactory.stdOutVeryVerbose()))
            .platformPieModule(new PlatformPieModule(PieBuilderImpl::new))
            .resourceServiceComponent(resourceServiceComponent)
            .build();
        final CharsComponent component = DaggerCharsComponent.builder()
            .charsResourcesComponent(resourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .build();
        try(final MixedSession session = component.getPie().newSession()) {
            final CommandFeedback feedback = session.require(component.getCharsShowAst().createTask(new CharsShowAst.Args(new FSPath(args[0]))));
            if(feedback.hasErrorMessages()) {
                System.out.println("ERRORS: " + feedback.getMessages().toString());
            }
            if(feedback.hasException()) {
                throw feedback.getException();
            }
            for(final ShowFeedback showFeedback : feedback.getShowFeedbacks()) {
                showFeedback.caseOf().showText((t, n, r) -> {
                    System.out.println(t);
                    return Optional.empty();
                }).otherwiseEmpty();
            }
        }
    }
}
