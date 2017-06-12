package mb.pipe.run.ceres.util

import java.io.Serializable

interface Tuple1<out A>: Serializable {
  operator fun component1(): A
}
data class TupleImpl1<out A>(val _1: A): Tuple1<A>
fun <A> tuple(_1: A): Tuple1<A> = TupleImpl1(_1)

interface Tuple2<out A, out B>: Serializable {
  operator fun component1(): A
  operator fun component2(): B
}
data class TupleImpl2<out A, out B>(val _1: A, val _2: B): Tuple2<A, B>
fun <A, B> tuple(_1: A, _2: B): Tuple2<A, B> = TupleImpl2(_1, _2)

interface Tuple3<out A, out B, out C>: Serializable {
  operator fun component1(): A
  operator fun component2(): B
  operator fun component3(): C
}
data class TupleImpl3<out A, out B, out C>(val _1: A, val _2: B, val _3: C): Tuple3<A, B, C>
fun <A, B, C> tuple(_1: A, _2: B, _3: C): Tuple3<A, B, C> = TupleImpl3(_1, _2, _3)

interface Tuple4<out A, out B, out C, out D>: Serializable {
  operator fun component1(): A
  operator fun component2(): B
  operator fun component3(): C
  operator fun component4(): D
}
data class TupleImpl4<out A, out B, out C, out D>(val _1: A, val _2: B, val _3: C, val _4: D): Tuple4<A, B, C, D>
fun <A, B, C, D> tuple(_1: A, _2: B, _3: C, _4: D): Tuple4<A, B, C, D> = TupleImpl4(_1, _2, _3, _4)

interface Tuple5<out A, out B, out C, out D, out E>: Serializable {
  operator fun component1(): A
  operator fun component2(): B
  operator fun component3(): C
  operator fun component4(): D
  operator fun component5(): E
}
data class TupleImpl5<out A, out B, out C, out D, out E>(val _1: A, val _2: B, val _3: C, val _4: D, val _5: E): Tuple5<A, B, C, D, E>
fun <A, B, C, D, E> tuple(_1: A, _2: B, _3: C, _4: D, _5: E): Tuple5<A, B, C, D, E> = TupleImpl5(_1, _2, _3, _4, _5)

interface Tuple6<out A, out B, out C, out D, out E, out F>: Serializable {
  operator fun component1(): A
  operator fun component2(): B
  operator fun component3(): C
  operator fun component4(): D
  operator fun component5(): E
  operator fun component6(): F
}
data class TupleImpl6<out A, out B, out C, out D, out E, out F>(val _1: A, val _2: B, val _3: C, val _4: D, val _5: E, val _6: F): Tuple6<A, B, C, D, E, F>
fun <A, B, C, D, E, F> tuple(_1: A, _2: B, _3: C, _4: D, _5: E, _6: F): Tuple6<A, B, C, D, E, F> = TupleImpl6(_1, _2, _3, _4, _5, _6)

interface Tuple7<out A, out B, out C, out D, out E, out F, out G>: Serializable {
  operator fun component1(): A
  operator fun component2(): B
  operator fun component3(): C
  operator fun component4(): D
  operator fun component5(): E
  operator fun component6(): F
  operator fun component7(): G
}
data class TupleImpl7<out A, out B, out C, out D, out E, out F, out G>(val _1: A, val _2: B, val _3: C, val _4: D, val _5: E, val _6: F, val _7: G): Tuple7<A, B, C, D, E, F, G>
fun <A, B, C, D, E, F, G> tuple(_1: A, _2: B, _3: C, _4: D, _5: E, _6: F, _7: G): Tuple7<A, B, C, D, E, F, G> = TupleImpl7(_1, _2, _3, _4, _5, _6, _7)

interface Tuple8<out A, out B, out C, out D, out E, out F, out G, out H>: Serializable {
  operator fun component1(): A
  operator fun component2(): B
  operator fun component3(): C
  operator fun component4(): D
  operator fun component5(): E
  operator fun component6(): F
  operator fun component7(): G
  operator fun component8(): H
}
data class TupleImpl8<out A, out B, out C, out D, out E, out F, out G, out H>(val _1: A, val _2: B, val _3: C, val _4: D, val _5: E, val _6: F, val _7: G, val _8: H): Tuple8<A, B, C, D, E, F, G, H>
fun <A, B, C, D, E, F, G, H> tuple(_1: A, _2: B, _3: C, _4: D, _5: E, _6: F, _7: G, _8: H): Tuple8<A, B, C, D, E, F, G, H> = TupleImpl8(_1, _2, _3, _4, _5, _6, _7, _8)

interface Tuple9<out A, out B, out C, out D, out E, out F, out G, out H, out I>: Serializable {
  operator fun component1(): A
  operator fun component2(): B
  operator fun component3(): C
  operator fun component4(): D
  operator fun component5(): E
  operator fun component6(): F
  operator fun component7(): G
  operator fun component8(): H
  operator fun component9(): I
}
data class TupleImpl9<out A, out B, out C, out D, out E, out F, out G, out H, out I>(val _1: A, val _2: B, val _3: C, val _4: D, val _5: E, val _6: F, val _7: G, val _8: H, val _9: I): Tuple9<A, B, C, D, E, F, G, H, I>
fun <A, B, C, D, E, F, G, H, I> tuple(_1: A, _2: B, _3: C, _4: D, _5: E, _6: F, _7: G, _8: H, _9: I): Tuple9<A, B, C, D, E, F, G, H, I> = TupleImpl9(_1, _2, _3, _4, _5, _6, _7, _8, _9)

interface Tuple10<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J>: Serializable {
  operator fun component1(): A
  operator fun component2(): B
  operator fun component3(): C
  operator fun component4(): D
  operator fun component5(): E
  operator fun component6(): F
  operator fun component7(): G
  operator fun component8(): H
  operator fun component9(): I
  operator fun component10(): J
}
data class TupleImpl10<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J>(val _1: A, val _2: B, val _3: C, val _4: D, val _5: E, val _6: F, val _7: G, val _8: H, val _9: I, val _10: J): Tuple10<A, B, C, D, E, F, G, H, I, J>
fun <A, B, C, D, E, F, G, H, I, J> tuple(_1: A, _2: B, _3: C, _4: D, _5: E, _6: F, _7: G, _8: H, _9: I, _10: J): Tuple10<A, B, C, D, E, F, G, H, I, J> = TupleImpl10(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10)

interface Tuple11<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K>: Serializable {
  operator fun component1(): A
  operator fun component2(): B
  operator fun component3(): C
  operator fun component4(): D
  operator fun component5(): E
  operator fun component6(): F
  operator fun component7(): G
  operator fun component8(): H
  operator fun component9(): I
  operator fun component10(): J
  operator fun component11(): K
}
data class TupleImpl11<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K>(val _1: A, val _2: B, val _3: C, val _4: D, val _5: E, val _6: F, val _7: G, val _8: H, val _9: I, val _10: J, val _11: K): Tuple11<A, B, C, D, E, F, G, H, I, J, K>
fun <A, B, C, D, E, F, G, H, I, J, K> tuple(_1: A, _2: B, _3: C, _4: D, _5: E, _6: F, _7: G, _8: H, _9: I, _10: J, _11: K): Tuple11<A, B, C, D, E, F, G, H, I, J, K> = TupleImpl11(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11)

interface Tuple12<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L>: Serializable {
  operator fun component1(): A
  operator fun component2(): B
  operator fun component3(): C
  operator fun component4(): D
  operator fun component5(): E
  operator fun component6(): F
  operator fun component7(): G
  operator fun component8(): H
  operator fun component9(): I
  operator fun component10(): J
  operator fun component11(): K
  operator fun component12(): L
}
data class TupleImpl12<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L>(val _1: A, val _2: B, val _3: C, val _4: D, val _5: E, val _6: F, val _7: G, val _8: H, val _9: I, val _10: J, val _11: K, val _12: L): Tuple12<A, B, C, D, E, F, G, H, I, J, K, L>
fun <A, B, C, D, E, F, G, H, I, J, K, L> tuple(_1: A, _2: B, _3: C, _4: D, _5: E, _6: F, _7: G, _8: H, _9: I, _10: J, _11: K, _12: L): Tuple12<A, B, C, D, E, F, G, H, I, J, K, L> = TupleImpl12(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12)

interface Tuple13<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M>: Serializable {
  operator fun component1(): A
  operator fun component2(): B
  operator fun component3(): C
  operator fun component4(): D
  operator fun component5(): E
  operator fun component6(): F
  operator fun component7(): G
  operator fun component8(): H
  operator fun component9(): I
  operator fun component10(): J
  operator fun component11(): K
  operator fun component12(): L
  operator fun component13(): M
}
data class TupleImpl13<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M>(val _1: A, val _2: B, val _3: C, val _4: D, val _5: E, val _6: F, val _7: G, val _8: H, val _9: I, val _10: J, val _11: K, val _12: L, val _13: M): Tuple13<A, B, C, D, E, F, G, H, I, J, K, L, M>
fun <A, B, C, D, E, F, G, H, I, J, K, L, M> tuple(_1: A, _2: B, _3: C, _4: D, _5: E, _6: F, _7: G, _8: H, _9: I, _10: J, _11: K, _12: L, _13: M): Tuple13<A, B, C, D, E, F, G, H, I, J, K, L, M> = TupleImpl13(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13)

interface Tuple14<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M, out N>: Serializable {
  operator fun component1(): A
  operator fun component2(): B
  operator fun component3(): C
  operator fun component4(): D
  operator fun component5(): E
  operator fun component6(): F
  operator fun component7(): G
  operator fun component8(): H
  operator fun component9(): I
  operator fun component10(): J
  operator fun component11(): K
  operator fun component12(): L
  operator fun component13(): M
  operator fun component14(): N
}
data class TupleImpl14<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M, out N>(val _1: A, val _2: B, val _3: C, val _4: D, val _5: E, val _6: F, val _7: G, val _8: H, val _9: I, val _10: J, val _11: K, val _12: L, val _13: M, val _14: N): Tuple14<A, B, C, D, E, F, G, H, I, J, K, L, M, N>
fun <A, B, C, D, E, F, G, H, I, J, K, L, M, N> tuple(_1: A, _2: B, _3: C, _4: D, _5: E, _6: F, _7: G, _8: H, _9: I, _10: J, _11: K, _12: L, _13: M, _14: N): Tuple14<A, B, C, D, E, F, G, H, I, J, K, L, M, N> = TupleImpl14(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14)

interface Tuple15<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M, out N, out O>: Serializable {
  operator fun component1(): A
  operator fun component2(): B
  operator fun component3(): C
  operator fun component4(): D
  operator fun component5(): E
  operator fun component6(): F
  operator fun component7(): G
  operator fun component8(): H
  operator fun component9(): I
  operator fun component10(): J
  operator fun component11(): K
  operator fun component12(): L
  operator fun component13(): M
  operator fun component14(): N
  operator fun component15(): O
}
data class TupleImpl15<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M, out N, out O>(val _1: A, val _2: B, val _3: C, val _4: D, val _5: E, val _6: F, val _7: G, val _8: H, val _9: I, val _10: J, val _11: K, val _12: L, val _13: M, val _14: N, val _15: O): Tuple15<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O>
fun <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O> tuple(_1: A, _2: B, _3: C, _4: D, _5: E, _6: F, _7: G, _8: H, _9: I, _10: J, _11: K, _12: L, _13: M, _14: N, _15: O): Tuple15<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O> = TupleImpl15(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15)

interface Tuple16<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M, out N, out O, out P>: Serializable {
  operator fun component1(): A
  operator fun component2(): B
  operator fun component3(): C
  operator fun component4(): D
  operator fun component5(): E
  operator fun component6(): F
  operator fun component7(): G
  operator fun component8(): H
  operator fun component9(): I
  operator fun component10(): J
  operator fun component11(): K
  operator fun component12(): L
  operator fun component13(): M
  operator fun component14(): N
  operator fun component15(): O
  operator fun component16(): P
}
data class TupleImpl16<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M, out N, out O, out P>(val _1: A, val _2: B, val _3: C, val _4: D, val _5: E, val _6: F, val _7: G, val _8: H, val _9: I, val _10: J, val _11: K, val _12: L, val _13: M, val _14: N, val _15: O, val _16: P): Tuple16<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P>
fun <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P> tuple(_1: A, _2: B, _3: C, _4: D, _5: E, _6: F, _7: G, _8: H, _9: I, _10: J, _11: K, _12: L, _13: M, _14: N, _15: O, _16: P): Tuple16<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P> = TupleImpl16(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15, _16)

interface Tuple17<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M, out N, out O, out P, out Q>: Serializable {
  operator fun component1(): A
  operator fun component2(): B
  operator fun component3(): C
  operator fun component4(): D
  operator fun component5(): E
  operator fun component6(): F
  operator fun component7(): G
  operator fun component8(): H
  operator fun component9(): I
  operator fun component10(): J
  operator fun component11(): K
  operator fun component12(): L
  operator fun component13(): M
  operator fun component14(): N
  operator fun component15(): O
  operator fun component16(): P
  operator fun component17(): Q
}
data class TupleImpl17<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M, out N, out O, out P, out Q>(val _1: A, val _2: B, val _3: C, val _4: D, val _5: E, val _6: F, val _7: G, val _8: H, val _9: I, val _10: J, val _11: K, val _12: L, val _13: M, val _14: N, val _15: O, val _16: P, val _17: Q): Tuple17<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q>
fun <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q> tuple(_1: A, _2: B, _3: C, _4: D, _5: E, _6: F, _7: G, _8: H, _9: I, _10: J, _11: K, _12: L, _13: M, _14: N, _15: O, _16: P, _17: Q): Tuple17<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q> = TupleImpl17(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15, _16, _17)

interface Tuple18<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M, out N, out O, out P, out Q, out R>: Serializable {
  operator fun component1(): A
  operator fun component2(): B
  operator fun component3(): C
  operator fun component4(): D
  operator fun component5(): E
  operator fun component6(): F
  operator fun component7(): G
  operator fun component8(): H
  operator fun component9(): I
  operator fun component10(): J
  operator fun component11(): K
  operator fun component12(): L
  operator fun component13(): M
  operator fun component14(): N
  operator fun component15(): O
  operator fun component16(): P
  operator fun component17(): Q
  operator fun component18(): R
}
data class TupleImpl18<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M, out N, out O, out P, out Q, out R>(val _1: A, val _2: B, val _3: C, val _4: D, val _5: E, val _6: F, val _7: G, val _8: H, val _9: I, val _10: J, val _11: K, val _12: L, val _13: M, val _14: N, val _15: O, val _16: P, val _17: Q, val _18: R): Tuple18<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R>
fun <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R> tuple(_1: A, _2: B, _3: C, _4: D, _5: E, _6: F, _7: G, _8: H, _9: I, _10: J, _11: K, _12: L, _13: M, _14: N, _15: O, _16: P, _17: Q, _18: R): Tuple18<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R> = TupleImpl18(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15, _16, _17, _18)

interface Tuple19<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M, out N, out O, out P, out Q, out R, out S>: Serializable {
  operator fun component1(): A
  operator fun component2(): B
  operator fun component3(): C
  operator fun component4(): D
  operator fun component5(): E
  operator fun component6(): F
  operator fun component7(): G
  operator fun component8(): H
  operator fun component9(): I
  operator fun component10(): J
  operator fun component11(): K
  operator fun component12(): L
  operator fun component13(): M
  operator fun component14(): N
  operator fun component15(): O
  operator fun component16(): P
  operator fun component17(): Q
  operator fun component18(): R
  operator fun component19(): S
}
data class TupleImpl19<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M, out N, out O, out P, out Q, out R, out S>(val _1: A, val _2: B, val _3: C, val _4: D, val _5: E, val _6: F, val _7: G, val _8: H, val _9: I, val _10: J, val _11: K, val _12: L, val _13: M, val _14: N, val _15: O, val _16: P, val _17: Q, val _18: R, val _19: S): Tuple19<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S>
fun <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S> tuple(_1: A, _2: B, _3: C, _4: D, _5: E, _6: F, _7: G, _8: H, _9: I, _10: J, _11: K, _12: L, _13: M, _14: N, _15: O, _16: P, _17: Q, _18: R, _19: S): Tuple19<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S> = TupleImpl19(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15, _16, _17, _18, _19)

interface Tuple20<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M, out N, out O, out P, out Q, out R, out S, out T>: Serializable {
  operator fun component1(): A
  operator fun component2(): B
  operator fun component3(): C
  operator fun component4(): D
  operator fun component5(): E
  operator fun component6(): F
  operator fun component7(): G
  operator fun component8(): H
  operator fun component9(): I
  operator fun component10(): J
  operator fun component11(): K
  operator fun component12(): L
  operator fun component13(): M
  operator fun component14(): N
  operator fun component15(): O
  operator fun component16(): P
  operator fun component17(): Q
  operator fun component18(): R
  operator fun component19(): S
  operator fun component20(): T
}
data class TupleImpl20<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M, out N, out O, out P, out Q, out R, out S, out T>(val _1: A, val _2: B, val _3: C, val _4: D, val _5: E, val _6: F, val _7: G, val _8: H, val _9: I, val _10: J, val _11: K, val _12: L, val _13: M, val _14: N, val _15: O, val _16: P, val _17: Q, val _18: R, val _19: S, val _20: T): Tuple20<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T>
fun <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T> tuple(_1: A, _2: B, _3: C, _4: D, _5: E, _6: F, _7: G, _8: H, _9: I, _10: J, _11: K, _12: L, _13: M, _14: N, _15: O, _16: P, _17: Q, _18: R, _19: S, _20: T): Tuple20<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T> = TupleImpl20(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15, _16, _17, _18, _19, _20)

interface Tuple21<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M, out N, out O, out P, out Q, out R, out S, out T, out U>: Serializable {
  operator fun component1(): A
  operator fun component2(): B
  operator fun component3(): C
  operator fun component4(): D
  operator fun component5(): E
  operator fun component6(): F
  operator fun component7(): G
  operator fun component8(): H
  operator fun component9(): I
  operator fun component10(): J
  operator fun component11(): K
  operator fun component12(): L
  operator fun component13(): M
  operator fun component14(): N
  operator fun component15(): O
  operator fun component16(): P
  operator fun component17(): Q
  operator fun component18(): R
  operator fun component19(): S
  operator fun component20(): T
  operator fun component21(): U
}
data class TupleImpl21<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M, out N, out O, out P, out Q, out R, out S, out T, out U>(val _1: A, val _2: B, val _3: C, val _4: D, val _5: E, val _6: F, val _7: G, val _8: H, val _9: I, val _10: J, val _11: K, val _12: L, val _13: M, val _14: N, val _15: O, val _16: P, val _17: Q, val _18: R, val _19: S, val _20: T, val _21: U): Tuple21<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U>
fun <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U> tuple(_1: A, _2: B, _3: C, _4: D, _5: E, _6: F, _7: G, _8: H, _9: I, _10: J, _11: K, _12: L, _13: M, _14: N, _15: O, _16: P, _17: Q, _18: R, _19: S, _20: T, _21: U): Tuple21<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U> = TupleImpl21(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15, _16, _17, _18, _19, _20, _21)

interface Tuple22<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M, out N, out O, out P, out Q, out R, out S, out T, out U, out V>: Serializable {
  operator fun component1(): A
  operator fun component2(): B
  operator fun component3(): C
  operator fun component4(): D
  operator fun component5(): E
  operator fun component6(): F
  operator fun component7(): G
  operator fun component8(): H
  operator fun component9(): I
  operator fun component10(): J
  operator fun component11(): K
  operator fun component12(): L
  operator fun component13(): M
  operator fun component14(): N
  operator fun component15(): O
  operator fun component16(): P
  operator fun component17(): Q
  operator fun component18(): R
  operator fun component19(): S
  operator fun component20(): T
  operator fun component21(): U
  operator fun component22(): V
}
data class TupleImpl22<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M, out N, out O, out P, out Q, out R, out S, out T, out U, out V>(val _1: A, val _2: B, val _3: C, val _4: D, val _5: E, val _6: F, val _7: G, val _8: H, val _9: I, val _10: J, val _11: K, val _12: L, val _13: M, val _14: N, val _15: O, val _16: P, val _17: Q, val _18: R, val _19: S, val _20: T, val _21: U, val _22: V): Tuple22<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V>
fun <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V> tuple(_1: A, _2: B, _3: C, _4: D, _5: E, _6: F, _7: G, _8: H, _9: I, _10: J, _11: K, _12: L, _13: M, _14: N, _15: O, _16: P, _17: Q, _18: R, _19: S, _20: T, _21: U, _22: V): Tuple22<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V> = TupleImpl22(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15, _16, _17, _18, _19, _20, _21, _22)

interface Tuple23<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M, out N, out O, out P, out Q, out R, out S, out T, out U, out V, out W>: Serializable {
  operator fun component1(): A
  operator fun component2(): B
  operator fun component3(): C
  operator fun component4(): D
  operator fun component5(): E
  operator fun component6(): F
  operator fun component7(): G
  operator fun component8(): H
  operator fun component9(): I
  operator fun component10(): J
  operator fun component11(): K
  operator fun component12(): L
  operator fun component13(): M
  operator fun component14(): N
  operator fun component15(): O
  operator fun component16(): P
  operator fun component17(): Q
  operator fun component18(): R
  operator fun component19(): S
  operator fun component20(): T
  operator fun component21(): U
  operator fun component22(): V
  operator fun component23(): W
}
data class TupleImpl23<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M, out N, out O, out P, out Q, out R, out S, out T, out U, out V, out W>(val _1: A, val _2: B, val _3: C, val _4: D, val _5: E, val _6: F, val _7: G, val _8: H, val _9: I, val _10: J, val _11: K, val _12: L, val _13: M, val _14: N, val _15: O, val _16: P, val _17: Q, val _18: R, val _19: S, val _20: T, val _21: U, val _22: V, val _23: W): Tuple23<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W>
fun <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W> tuple(_1: A, _2: B, _3: C, _4: D, _5: E, _6: F, _7: G, _8: H, _9: I, _10: J, _11: K, _12: L, _13: M, _14: N, _15: O, _16: P, _17: Q, _18: R, _19: S, _20: T, _21: U, _22: V, _23: W): Tuple23<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W> = TupleImpl23(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15, _16, _17, _18, _19, _20, _21, _22, _23)

interface Tuple24<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M, out N, out O, out P, out Q, out R, out S, out T, out U, out V, out W, out X>: Serializable {
  operator fun component1(): A
  operator fun component2(): B
  operator fun component3(): C
  operator fun component4(): D
  operator fun component5(): E
  operator fun component6(): F
  operator fun component7(): G
  operator fun component8(): H
  operator fun component9(): I
  operator fun component10(): J
  operator fun component11(): K
  operator fun component12(): L
  operator fun component13(): M
  operator fun component14(): N
  operator fun component15(): O
  operator fun component16(): P
  operator fun component17(): Q
  operator fun component18(): R
  operator fun component19(): S
  operator fun component20(): T
  operator fun component21(): U
  operator fun component22(): V
  operator fun component23(): W
  operator fun component24(): X
}
data class TupleImpl24<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M, out N, out O, out P, out Q, out R, out S, out T, out U, out V, out W, out X>(val _1: A, val _2: B, val _3: C, val _4: D, val _5: E, val _6: F, val _7: G, val _8: H, val _9: I, val _10: J, val _11: K, val _12: L, val _13: M, val _14: N, val _15: O, val _16: P, val _17: Q, val _18: R, val _19: S, val _20: T, val _21: U, val _22: V, val _23: W, val _24: X): Tuple24<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X>
fun <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X> tuple(_1: A, _2: B, _3: C, _4: D, _5: E, _6: F, _7: G, _8: H, _9: I, _10: J, _11: K, _12: L, _13: M, _14: N, _15: O, _16: P, _17: Q, _18: R, _19: S, _20: T, _21: U, _22: V, _23: W, _24: X): Tuple24<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X> = TupleImpl24(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15, _16, _17, _18, _19, _20, _21, _22, _23, _24)

interface Tuple25<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M, out N, out O, out P, out Q, out R, out S, out T, out U, out V, out W, out X, out Y>: Serializable {
  operator fun component1(): A
  operator fun component2(): B
  operator fun component3(): C
  operator fun component4(): D
  operator fun component5(): E
  operator fun component6(): F
  operator fun component7(): G
  operator fun component8(): H
  operator fun component9(): I
  operator fun component10(): J
  operator fun component11(): K
  operator fun component12(): L
  operator fun component13(): M
  operator fun component14(): N
  operator fun component15(): O
  operator fun component16(): P
  operator fun component17(): Q
  operator fun component18(): R
  operator fun component19(): S
  operator fun component20(): T
  operator fun component21(): U
  operator fun component22(): V
  operator fun component23(): W
  operator fun component24(): X
  operator fun component25(): Y
}
data class TupleImpl25<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M, out N, out O, out P, out Q, out R, out S, out T, out U, out V, out W, out X, out Y>(val _1: A, val _2: B, val _3: C, val _4: D, val _5: E, val _6: F, val _7: G, val _8: H, val _9: I, val _10: J, val _11: K, val _12: L, val _13: M, val _14: N, val _15: O, val _16: P, val _17: Q, val _18: R, val _19: S, val _20: T, val _21: U, val _22: V, val _23: W, val _24: X, val _25: Y): Tuple25<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y>
fun <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y> tuple(_1: A, _2: B, _3: C, _4: D, _5: E, _6: F, _7: G, _8: H, _9: I, _10: J, _11: K, _12: L, _13: M, _14: N, _15: O, _16: P, _17: Q, _18: R, _19: S, _20: T, _21: U, _22: V, _23: W, _24: X, _25: Y): Tuple25<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y> = TupleImpl25(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15, _16, _17, _18, _19, _20, _21, _22, _23, _24, _25)

interface Tuple26<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M, out N, out O, out P, out Q, out R, out S, out T, out U, out V, out W, out X, out Y, out Z>: Serializable {
  operator fun component1(): A
  operator fun component2(): B
  operator fun component3(): C
  operator fun component4(): D
  operator fun component5(): E
  operator fun component6(): F
  operator fun component7(): G
  operator fun component8(): H
  operator fun component9(): I
  operator fun component10(): J
  operator fun component11(): K
  operator fun component12(): L
  operator fun component13(): M
  operator fun component14(): N
  operator fun component15(): O
  operator fun component16(): P
  operator fun component17(): Q
  operator fun component18(): R
  operator fun component19(): S
  operator fun component20(): T
  operator fun component21(): U
  operator fun component22(): V
  operator fun component23(): W
  operator fun component24(): X
  operator fun component25(): Y
  operator fun component26(): Z
}
data class TupleImpl26<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J, out K, out L, out M, out N, out O, out P, out Q, out R, out S, out T, out U, out V, out W, out X, out Y, out Z>(val _1: A, val _2: B, val _3: C, val _4: D, val _5: E, val _6: F, val _7: G, val _8: H, val _9: I, val _10: J, val _11: K, val _12: L, val _13: M, val _14: N, val _15: O, val _16: P, val _17: Q, val _18: R, val _19: S, val _20: T, val _21: U, val _22: V, val _23: W, val _24: X, val _25: Y, val _26: Z): Tuple26<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z>
fun <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z> tuple(_1: A, _2: B, _3: C, _4: D, _5: E, _6: F, _7: G, _8: H, _9: I, _10: J, _11: K, _12: L, _13: M, _14: N, _15: O, _16: P, _17: Q, _18: R, _19: S, _20: T, _21: U, _22: V, _23: W, _24: X, _25: Y, _26: Z): Tuple26<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z> = TupleImpl26(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15, _16, _17, _18, _19, _20, _21, _22, _23, _24, _25, _26)
