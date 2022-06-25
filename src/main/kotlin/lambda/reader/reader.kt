package lambda.reader

import lambda.expression.Expression
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker

class DefaultListener : LambdaBaseListener() {

    private val stack = ArrayDeque<Expression>()

    private var createName = true

    private val definitions = HashMap<String, Expression>()

    private val boundVariables = ArrayDeque<String>()

    override fun enterDefinition(ctx: LambdaParser.DefinitionContext) {
        createName = false
    }

    override fun exitDefinition(ctx: LambdaParser.DefinitionContext) {
        val name = ctx.name()
        val expression = stack.removeLast()
        definitions[name.text] = expression
    }

    override fun enterName(ctx: LambdaParser.NameContext) {
        if (createName) {
            val name = ctx.text
            val expression = if (isFreeVariable(name)) {
                checkNotNull(definitions[name]) { "Unresolved name: $name" }
            } else {
                Expression.Name(name)
            }
            stack.addLast(expression)
        }
    }

    override fun enterFunction(ctx: LambdaParser.FunctionContext) {
        createName = false
        val variable = ctx.name()
        boundVariables.addLast(variable.text)
    }

    override fun exitFunction(ctx: LambdaParser.FunctionContext) {
        val name = ctx.name()
        val body = stack.removeLast()
        stack.addLast(Expression.Function(name.text, body))
        boundVariables.removeLast()
    }

    override fun enterExpression(ctx: LambdaParser.ExpressionContext?) {
        createName = true
    }

    override fun exitApplication(ctx: LambdaParser.ApplicationContext) {
        val argument = stack.removeLast()
        val function = stack.removeLast()
        stack.addLast(Expression.Application(function, argument))
    }

    private fun isFreeVariable(variable: String) =
        boundVariables.lastIndexOf(variable) == -1

    val expression: Expression
        get() = stack.single()
}

object Reader {
    fun read(input: String): Expression {
        val charStream = CharStreams.fromString(input)
        val lexer = LambdaLexer(charStream)
        val tokenStream = CommonTokenStream(lexer)
        val parser = LambdaParser(tokenStream)
        val listener = DefaultListener()
        val tree = parser.file()
        val walker = ParseTreeWalker()
        walker.walk(listener, tree)
        return listener.expression
    }
}
