package mb.statix.referenceretention.strategies.runtime;

import mb.log.slf4j.SLF4JLoggerFactory;
import mb.nabl2.terms.ITerm;
import mb.statix.referenceretention.statix.LockedReference;
import mb.statix.referenceretention.tego.RRContext;
import mb.statix.referenceretention.tego.RRSolverState;
import mb.statix.referenceretention.tego.UnwrapOrFixAllReferencesStrategy;
import mb.tego.strategies.Strategy1;
import mb.tego.strategies.runtime.TegoRuntime;
import mb.tego.strategies.runtime.TegoRuntimeImpl;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static mb.tego.strategies.StrategyExt.fun;
import static org.junit.jupiter.api.Assertions.assertTrue;

//public class UnwrapOrFixAllReferencesStrategyTests {
//
//    @SuppressWarnings("resource") @Test
//    void test() throws InterruptedException {
//        // Arrange
//        final UnwrapOrFixAllReferencesStrategy strategy = UnwrapOrFixAllReferencesStrategy.getInstance();
//        final SLF4JLoggerFactory loggerFactory = new SLF4JLoggerFactory();
//        final TegoRuntime runtime = new TegoRuntimeImpl(loggerFactory);
//        final Strategy1<ITerm, LockedReference, @Nullable ITerm> qualifyReferenceStrategy = fun((input, arg1) -> {
//            // TODO: Implement!
//            return input;
//        });
//        final RRContext ctx = new RRContext(qualifyReferenceStrategy, Collections.emptyList());
//        final RRSolverState input = null; // TODO: RRSolverState.fromSolverResult();
//
//        // Act
////        @Nullable final Seq<RRSolverState> resultSeq = runtime.eval(strategy, ctx, input);
////        @Nullable final List<RRSolverState> result = (resultSeq != null ? resultSeq.toList() : null);
//
//        // Assert
//        assertTrue(true);
//    }
//}
