package lambda.reader

import lambda.expression.Expression
import lambda.expression.Expression.Companion.application
import lambda.expression.Expression.Companion.function
import lambda.expression.Expression.Companion.name
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class ReaderTest {

    @ParameterizedTest
    @MethodSource("shouldReadFunction")
    internal fun shouldReadFunction(expression: Expression, string: String) {
        assertEquals(expression, Reader.read(string))
    }

    @ParameterizedTest
    @MethodSource("shouldReadApplication")
    internal fun shouldReadApplication(expression: Expression, string: String) {
        assertEquals(expression, Reader.read(string))
    }

    @ParameterizedTest
    @MethodSource("shouldReadDefinition")
    internal fun shouldReadDefinition(expression: Expression, string: String) {
        assertEquals(expression, Reader.read(string))
    }

    companion object {

        @JvmStatic
        private fun shouldReadFunction(): Stream<Arguments> =
            Stream.of(
                Arguments.of(function("x", name("x")), "λx.x"),
                Arguments.of(function("fst", function("snd", name("fst"))), "λfst.λsnd.fst"),
                Arguments.of(function("f", function("a", application(name("f"), name("a")))), "λf.λa.(f a)")
            )

        @JvmStatic
        private fun shouldReadApplication(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    application(function("x", name("x")), function("a", function("b", name("b")))),
                    "(λx.x λa.λb.b)"
                ),
                Arguments.of(
                    application(
                        application(
                            function(
                                "f",
                                function(
                                    "a",
                                    application(name("f"), name("a"))
                                )
                            ),
                            function("s", application(name("s"), name("s")))
                        ),
                        function("x", name("x"))
                    ),
                    """
                        def identity = λx.x
                        def self_apply = λs.(s s)
                        def apply = λf.λa.(f a)
                        apply self_apply identity
                    """.trimIndent()
                )
            )

        @JvmStatic
        private fun shouldReadDefinition(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    function("x", name("x")),
                    """def identity = λx.x
                        identity""".trimIndent()
                ),
                Arguments.of(
                    application(
                        application(
                            function(
                                "f",
                                function(
                                    "a",
                                    application(name("f"), name("a"))
                                )
                            ),
                            function(
                                "x",
                                name("x")
                            )
                        ),
                        function(
                            "s",
                            application(name("s"), name("s"))
                        )
                    ),
                    """
                        def apply = λf.λa.(f a)
                        def a = λx.x
                        def b = λs.(s s)
                        ((apply a) b)
                    """.trimIndent()
                ),
                Arguments.of(
                    function(
                        "e1",
                        function(
                            "e2",
                            function(
                                "c",
                                application(application(name("c"), name("e1")), name("e2"))
                            )
                        )
                    ),
                    """def make_pair e1 e2 c = c e1 e2
                        make_pair
                        """.trimIndent()
                )
            )
    }
}
