package mb.statix.strategies.runtime;

//
///**
// * Concatenation strategy.
// *
// * This concatenates two lazy sequences into a new lazy sequence.
// *
// * @param <CTX> the type of context (invariant)
// * @param <T> the type of input (invariant)
// */
//public final class ConcatStrategy<CTX, T> extends NamedStrategy<CTX, Tuple2<Seq<T>, Seq<T>>, T> {
//
//    @SuppressWarnings("rawtypes")
//    private static final ConcatStrategy instance = new ConcatStrategy();
//    @SuppressWarnings("unchecked")
//    public static <CTX, T> ConcatStrategy<CTX, T> getInstance() { return (ConcatStrategy<CTX, T>)instance; }
//
//    private ConcatStrategy() { /* Prevent instantiation. Use getInstance(). */ }
//
//
//    @Override
//    public Seq<T> eval(CTX ctx, Tuple2<Seq<T>, Seq<T>> input) {
//        //   ?(x, y); or(!x, !y)
//        // = match({x, y}: (x, y), or(const(x), const(y)
//        AtomicReference<Seq<T>> x = new AtomicReference<>();
//        AtomicReference<Seq<T>> y = new AtomicReference<>();
//        //noinspection unchecked
//        return SeqStrategy.getInstance().apply(
//            MatchStrategy.getInstance().apply(new Tuple2Pattern<CTX, T, T>(
//                (o, it) -> { //noinspection unchecked
//                    x.set((Seq<T>)it); return true; },
//                (o, it) -> { //noinspection unchecked
//                    y.set((Seq<T>)it); return true; }
//            )),
//            OrStrategy.getInstance().apply(
//                ConstStrategy.getInstance().apply(x),
//                ConstStrategy.getInstance().apply(y)
//            )
//        ).eval(ctx, input);
//    }
//
//    @Override
//    public String getName() {
//        return "concat";
//    }
//
//    @Override
//    public String getParamName(int index) {
//        return super.getParamName(index);
//    }
//}
