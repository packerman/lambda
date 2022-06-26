package lambda.reader

import lambda.expression.Expression
import lambda.reader.LambdaParser.ExpressionContext
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTreeWalker
import kotlin.collections.set

class DefaultListener : LambdaBaseListener() {

    private val stack = ArrayDeque<Expression>()

    private var createName = true

    private val definitions = HashMap<String, Expression>()

    private val boundVariables = ArrayDeque<String>()

    private var currentDefinition: Definition? = null

    override fun enterDefinition(ctx: LambdaParser.DefinitionContext) {
        createName = false
        val nameTokens = ctx.NAME()
        val name = nameTokens[0].text
        val arguments = nameTokens
            .asSequence()
            .drop(1)
            .map { it.text }
            .toList()
        arguments.forEach(boundVariables::addLast)
        currentDefinition = Definition(name, arguments)
    }

    override fun exitDefinition(ctx: LambdaParser.DefinitionContext) {
        val definition = checkNotNull(currentDefinition)
        var expression = stack.removeLast()
        for (i in (definition.argumentCount - 1).downTo(0)) {
            expression = Expression.function(definition.argument(i), expression)
        }
        definitions[definition.name] = expression
        repeat(definition.argumentCount) {
            boundVariables.removeLast()
        }
        currentDefinition = null
    }

    override fun exitName(ctx: LambdaParser.NameContext) {
        val name = ctx.text
        if (createName) {
            val expression = if (isFreeVariable(name)) {
                checkNotNull(definitions[name]) { "Unresolved name: $name" }
            } else {
                Expression.name(name)
            }
            stack.addLast(expression)
        }
    }

    override fun enterFunction(ctx: LambdaParser.FunctionContext) {
        createName = false
        val variable = ctx.NAME()
        boundVariables.addLast(variable.text)
    }

    override fun exitFunction(ctx: LambdaParser.FunctionContext) {
        val name = ctx.NAME()
        val body = stack.removeLast()
        stack.addLast(Expression.function(name.text, body))
        boundVariables.removeLast()
    }

    override fun enterBody(ctx: LambdaParser.BodyContext) {
        createName = true
    }

    override fun exitApplication(ctx: LambdaParser.ApplicationContext) {
        applyExpressions(expressionChildCount(ctx))
    }

    override fun enterTop_level_expression(ctx: LambdaParser.Top_level_expressionContext) {
        createName = true
    }

    override fun exitTop_level_expression(ctx: LambdaParser.Top_level_expressionContext) {
        val count = expressionChildCount(ctx)
        if (count > 1) {
            applyExpressions(count)
        }
    }

    private fun applyExpressions(n: Int) {
        val children = ArrayDeque<Expression>()
        repeat(n) {
            children.addFirst(stack.removeLast())
        }
        val iterator = children.iterator()
        var application = Expression.application(iterator.next(), iterator.next())
        iterator.forEachRemaining { expression ->
            application = Expression.application(application, expression)
        }
        stack.addLast(application)
    }

    private fun isFreeVariable(variable: String) =
        boundVariables.lastIndexOf(variable) == -1

    val expression: Expression
        get() = stack.single()

    companion object {

        private fun expressionChildCount(ctx: ParserRuleContext): Int =
            ctx.children
                .asSequence()
                .filterIsInstance<ExpressionContext>()
                .count()
    }
}

private data class Definition(val name: String, val arguments: List<String>) {

    fun argument(i: Int) = arguments[i]

    val argumentCount = arguments.size
}

object Reader {
    fun read(input: String): Expression {
        val charStream = CharStreams.fromString(input)
        val lexer = LambdaLexer(charStream).apply {
            removeErrorListeners()
            addErrorListener(DefaultErrorListener)
        }
        val tokenStream = CommonTokenStream(lexer)
        val parser = LambdaParser(tokenStream).apply {
            removeErrorListeners()
            addErrorListener(DefaultErrorListener)
        }
        val listener = DefaultListener()
        val tree = parser.file()
        val walker = ParseTreeWalker()
        walker.walk(listener, tree)
        return listener.expression
    }
}

private object DefaultErrorListener : BaseErrorListener() {
    override fun syntaxError(
        recognizer: Recognizer<*, *>,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String,
        e: RecognitionException?
    ) {
        error("line $line:$charPositionInLine $msg")
    }
}
