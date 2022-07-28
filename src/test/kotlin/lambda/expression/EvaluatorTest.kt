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
    @MethodSource("shouldEvaluateExpressions", "shouldEvaluateBooleanExpressions", "shouldEvaluateNumberExpressions")
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
        fun shouldEvaluateBooleanExpressions(): Stream<Arguments> =
            Stream.of(
                Arguments.of("false", "not true"),
                Arguments.of("true", "not false"),
                Arguments.of("false", "and false false"),
                Arguments.of("false", "and false true"),
                Arguments.of("false", "and true false"),
                Arguments.of("true", "and true true"),
                Arguments.of("false", "or false false"),
                Arguments.of("true", "or false true"),
                Arguments.of("true", "or true false"),
                Arguments.of("true", "or true true"),
            )

        @JvmStatic
        fun shouldEvaluateNumberExpressions(): Stream<Arguments> =
            Stream.of(
                Arguments.of("identity", "0"),
                Arguments.of("λs.((s false) 0)", "1"),
                Arguments.of("λs.((s false) λs.((s false) 0))", "2"),
                Arguments.of("λs.((s false) λs.((s false) λs.((s false) 0)))", "3"),
                Arguments.of("1", "succ 0"),
                Arguments.of("2", "succ 1"),
                Arguments.of("3", "succ 2"),
                Arguments.of("true", "is_zero 0"),
                Arguments.of("false", "is_zero 1"),
                Arguments.of("false", "is_zero 2"),
                Arguments.of("0", "pred 1"),
                Arguments.of("1", "pred 2"),
                Arguments.of("2", "pred 3"),
                Arguments.of("0", "pred 0"),
            )

        @JvmStatic
        fun shouldExceedRecursionLimit(): Stream<Arguments> =
            Stream.of(
                Arguments.of("self_apply self_apply"),
                Arguments.of("(λh.((λa.λf.(f a) h) h) λf.(f f))")
            )
    }
}
