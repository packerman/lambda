package lambda.expression

import lambda.expression.Expression.Companion.application
import lambda.expression.Expression.Companion.function
import lambda.expression.Expression.Companion.name
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class ExpressionTest {

    @ParameterizedTest
    @MethodSource("shouldReplaceVariables")
    internal fun shouldReplaceVariables(
        expected: Expression,
        initial: Expression,
        variable: String,
        replacement: Expression
    ) {
        val actual = initial.replace(variable, replacement)
        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @MethodSource("shouldReduceExpressions")
    internal fun shouldReduceExpressions(expected: Expression, initial: Expression) {
        assertEquals(expected, initial.reduce(EmptyListener))
    }

    companion object {
        @JvmStatic
        fun shouldReplaceVariables(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    application(name("a"), function("f", name("f"))),
                    application(name("f"), function("f", name("f"))),
                    "f",
                    name("a")
                ),
                Arguments.of(
                    application(name("a"), function("x", name("a"))),
                    application(name("f"), function("x", name("f"))),
                    "f",
                    name("a")
                ),
                Arguments.of(
                    application(name("f"), function("f", name("f"))),
                    application(name("f"), function("f", name("f"))),
                    "x",
                    name("a")
                )
            )

        @JvmStatic
        fun shouldReduceExpressions(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    function("x", name("x")),
                    application(function("s", application(name("s"), name("s"))), function("x", name("x")))
                )
            )
    }
}
