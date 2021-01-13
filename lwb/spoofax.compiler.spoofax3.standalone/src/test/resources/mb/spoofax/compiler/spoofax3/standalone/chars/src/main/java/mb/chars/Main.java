package mb.chars;

import mb.log.stream.StreamLoggerFactory;
import mb.pie.api.MixedSession;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.fs.FSPath;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.spoofax.core.platform.DaggerPlatformComponent;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.PlatformPieModule;

import java.util.Optional;

public class Main {
    public static void main(String[] args) throws Exception {
        final PlatformComponent platformComponent = DaggerPlatformComponent.builder()
            .loggerFactoryModule(new LoggerFactoryModule(StreamLoggerFactory.stdOutVeryVerbose()))
            .platformPieModule(new PlatformPieModule(PieBuilderImpl::new))
            .build();
        final CharsComponent charsComponent = DaggerCharsComponent.builder()
            .platformComponent(platformComponent)
            .build();
        try(final MixedSession session = charsComponent.getPie().newSession()) {
            final CommandFeedback feedback = session.require(charsComponent.getCharsShowAst().createTask(new CharsShowAst.Args(new FSPath(args[0]))));
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
