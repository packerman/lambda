package lambda.expression

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class EvaluatorTest {

    private val evaluator = Evaluator.create()

    @ParameterizedTest
    @MethodSource("shouldEvaluateExpressions")
    internal fun shouldEvaluateExpressions(expected: String, initial: String) {
        assertEquals(
            evaluator.evaluate(expected, PrintListener()),
            evaluator.evaluate(initial, PrintListener())
        )
    }

    @ParameterizedTest
    @MethodSource("shouldExceedRecursionLimit")
    internal fun shouldExceedRecursionLimit(initial: String) {
        assertThrows<java.lang.IllegalStateException> {
            evaluator.evaluate(initial, EmptyListener)
        }
    }

    companion object {
        @JvmStatic
        fun shouldEvaluateExpressions(): Stream<Arguments> =
            Stream.of(
                Arguments.of("identity", "self_apply identity"),
                Arguments.of("identity", "(make_pair identity apply) select_first"),
                Arguments.of("apply", "(make_pair identity apply) select_second"),
                Arguments.of("\\p.\\q.p", "((\\x.\\y.(y x) \\p.\\q.p) \\i.i)"),
                Arguments.of("\\j.j", "(((\\x.\\y.\\z.((x y) z) \\f.\\a.(f a)) \\i.i) \\j.j)"),
                Arguments.of("\\b.\\k.k", "((\\p.\\q.(p q) (\\x.x \\a.\\b.a)) \\k.k)"),
                Arguments.of("\\b.b", "(((\\f.\\g.\\x.(f (g x)) \\s.(s s)) \\a.\\b.b) \\x.\\y.x)")
            )

        @JvmStatic
        fun shouldExceedRecursionLimit(): Stream<Arguments> =
            Stream.of(
                Arguments.of("self_apply self_apply"),
                Arguments.of("(\\h.((\\a.\\f.(f a) h) h) \\f.(f f))")
            )
    }
}
