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
                Arguments.of("λp.λq.p", "((λx.λy.(y x) λp.λq.p) λi.i)"),
                Arguments.of("λj.j", "(((λx.λy.λz.((x y) z) λf.λa.(f a)) λi.i) λj.j)"),
                Arguments.of("λb.λk.k", "((λp.λq.(p q) (λx.x λa.λb.a)) λk.k)"),
                Arguments.of("λb.b", "(((λf.λg.λx.(f (g x)) λs.(s s)) λa.λb.b) λx.λy.x)")
            )

        @JvmStatic
        fun shouldExceedRecursionLimit(): Stream<Arguments> =
            Stream.of(
                Arguments.of("self_apply self_apply"),
                Arguments.of("(\\h.((\\a.\\f.(f a) h) h) \\f.(f f))")
            )
    }
}
