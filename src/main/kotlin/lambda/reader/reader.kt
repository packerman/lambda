package lambda.reader

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

class DefaultVisitor: LambdaBaseVisitor<Expression>() {

    override fun visitName(ctx: LambdaParser.NameContext): Expression = Expression.Name(ctx.text)

    override fun visitFunction(ctx: LambdaParser.FunctionContext): Expression =
        Expression.Function(ctx.name().text, visitExpression(ctx.expression()))

    override fun visitApplication(ctx: LambdaParser.ApplicationContext): Expression =
        Expression.Application(visitExpression(ctx.expression(0)), visitExpression(ctx.expression(1)))
}

object Reader {
    fun read(input: String): Expression {
        val charStream = CharStreams.fromString(input)
        val lexer = LambdaLexer(charStream)
        val tokenStream = CommonTokenStream(lexer)
        val parser = LambdaParser(tokenStream)
        val expression = parser.expression()
        val visitor = DefaultVisitor()
        return visitor.visit(expression)
    }
}
