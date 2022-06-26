package lambda.reader

import lambda.expression.Expression
import lambda.reader.LambdaParser.ExpressionContext
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.collections.HashMap
import kotlin.collections.asSequence
import kotlin.collections.set
import kotlin.collections.single

class DefaultListener : LambdaBaseListener() {

    private val stack = ArrayDeque<Expression>()

    private var createName = true

    private val definitions = HashMap<String, Expression>()

    private val boundVariables = ArrayDeque<String>()

    override fun enterDefinition(ctx: LambdaParser.DefinitionContext) {
        createName = false
    }

    override fun exitDefinition(ctx: LambdaParser.DefinitionContext) {
        val name = ctx.NAME()
        val expression = stack.removeLast()
        definitions[name.text] = expression
    }

    override fun exitName(ctx: LambdaParser.NameContext) {
        if (createName) {
            val name = ctx.text
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
        val children = LinkedList<Expression>()
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

    private fun expressionChildCount(ctx: ParserRuleContext): Int =
        ctx.children
            .asSequence()
            .filterIsInstance<ExpressionContext>()
            .count()

    private fun isFreeVariable(variable: String) =
        boundVariables.lastIndexOf(variable) == -1

    val expression: Expression
        get() = stack.single()
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
        e: RecognitionException
    ) {
        error("line $line:$charPositionInLine $msg")
    }
}
