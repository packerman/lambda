package lambda.reader

import lambda.reader.Expression.*
import lambda.reader.Expression.Function
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class ReaderTest {

    @ParameterizedTest
    @MethodSource("shouldReadName")
    internal fun shouldReadName(string: String) {
        assertEquals(Name(string), Reader.read(string))
    }

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

    companion object {

        @JvmStatic
        private fun shouldReadName(): Stream<Arguments> =
            Stream.of(
                Arguments.of("abc"),
                Arguments.of("abc-123"),
                Arguments.of("123_abc_xyz"),
                Arguments.of("33"),
                Arguments.of("+"),
                Arguments.of("->")
            )

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
                Arguments.of(Application(Function("x", Name("x")), Function("a", Function("b", Name("b")))),
                    "(\\x.x \\a.\\b.b)")
            )
    }
}
