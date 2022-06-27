package lambda.expression

import lambda.reader.Reader
import java.io.InputStream

class Evaluator private constructor() {

    private val definitions = HashMap<String, Expression>()

    fun evaluate(input: String, listener: ReductionListener = EmptyListener): Expression? {
        val expression = Reader.read(input, definitions)
        return expression?.reduce(listener)
    }

    fun evaluate(input: InputStream): Expression? {
        val expression = Reader.read(input, definitions)
        return expression?.reduce(EmptyListener)
    }

    companion object {

        private const val STANDARD_DEFINITIONS = "/standard.lambda"

        fun create(): Evaluator {
            val evaluator = Evaluator()
            checkNotNull(
                Evaluator::class.java.getResourceAsStream(STANDARD_DEFINITIONS)
            ) { "Resource '$STANDARD_DEFINITIONS' not found" }
                .use(evaluator::evaluate)
            return evaluator
        }
    }
}

object EmptyListener : ReductionListener {
    override fun startedReduction(depth: Int, expression: Expression) {
    }

    override fun reduced(depth: Int, function: Expression, argument: Expression, result: Expression) {
    }
}

class PrintListener : ReductionListener {

    private var step = 0

    override fun startedReduction(depth: Int, expression: Expression) {
        println("(depth=$depth) evaluating $expression")
    }

    override fun reduced(depth: Int, function: Expression, argument: Expression, result: Expression) {
        println("(depth=$depth, step=$step) reduced ($function $argument) => $result")
        step++
    }
}
