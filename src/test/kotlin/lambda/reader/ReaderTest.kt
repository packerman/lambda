package lambda.reader

import lambda.expression.Expression
import lambda.expression.Expression.*
import lambda.expression.Expression.Function
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
                Arguments.of(Function("x", Name("x")), "\\x.x"),
                Arguments.of(Function("fst", Function("snd", Name("fst"))), "\\fst.\\snd.fst"),
                Arguments.of(Function("f", Function("a", Application(Name("f"), Name("a")))), "\\f.\\a.(f a)")
            )

        @JvmStatic
        private fun shouldReadApplication(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    Application(Function("x", Name("x")), Function("a", Function("b", Name("b")))),
                    "(\\x.x \\a.\\b.b)"
                ),
                Arguments.of(
                    Application(
                        Application(
                            Function(
                                "f",
                                Function(
                                    "a",
                                    Application(Name("f"), Name("a"))
                                )
                            ),
                            Function("s", Application(Name("s"), Name("s")))
                        ),
                        Function("x", Name("x"))
                    ),
                    """
                        def identity = \x.x
                        def self_apply = \s.(s s)
                        def apply = \f.\a.(f a)
                        apply self_apply identity
                    """.trimIndent()
                )
            )

        @JvmStatic
        private fun shouldReadDefinition(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    Function("x", Name("x")),
                    """def identity = \x.x
                        identity""".trimIndent()
                ),
                Arguments.of(
                    Application(
                        Application(
                            Function(
                                "f",
                                Function(
                                    "a",
                                    Application(Name("f"), Name("a"))
                                )
                            ),
                            Function(
                                "x",
                                Name("x")
                            )
                        ),
                        Function(
                            "s",
                            Application(Name("s"), Name("s"))
                        )
                    ),
                    """
                        def apply = \f.\a.(f a)
                        def a = \x.x
                        def b = \s.(s s)
                        ((apply a) b)
                    """.trimIndent()
                )
            )
    }
}
