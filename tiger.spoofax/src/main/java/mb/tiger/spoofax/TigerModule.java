package mb.tiger.spoofax;

import dagger.Module;
import dagger.Provides;
import mb.pie.api.exec.TopDownExecutor;
import mb.spoofax.core.language.AstService;
import mb.tiger.spoofax.pie.ParseTaskDef;

@Module
public class TigerModule {
    @Provides static AstService provideAstService(TopDownExecutor topDownExecutor, ParseTaskDef parseTaskDef) {
        return new TigerAstService(topDownExecutor, parseTaskDef);
    }
}
