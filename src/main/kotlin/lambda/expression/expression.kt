package lambda.expression

sealed class Expression {

    private data class Name(val name: String) : Expression() {
        override fun replace(variable: String, replacement: Expression): Expression =
            if (this.name == variable) replacement else this

        override fun toString() = name
    }

    private data class Function(val name: String, val body: Expression) : Expression() {
        override fun replace(variable: String, replacement: Expression): Expression =
            if (this.name == variable) this else function(this.name, body.replace(variable, replacement))

        override fun toString() = "λ$name.$body"
    }

    private data class Application(val function: Expression, val argument: Expression) : Expression() {
        override fun replace(variable: String, replacement: Expression): Expression =
            application(function.replace(variable, replacement), argument.replace(variable, replacement))

        override fun toString() = "($function $argument)"
    }

    abstract fun replace(variable: String, replacement: Expression): Expression

    fun reduce(listener: ReductionListener): Expression = Companion.reduce(this, 0, listener)

    companion object {

        fun name(name: String): Expression = Name(name)

        fun function(name: String, body: Expression): Expression = Function(name, body)

        fun application(function: Expression, argument: Expression): Expression = Application(function, argument)

        private const val RECURSION_DEPTH_LIMIT = 10000

        @Suppress("NON_TAIL_RECURSIVE_CALL")
        tailrec fun reduce(expression: Expression, depth: Int, listener: ReductionListener): Expression {
            check(depth <= RECURSION_DEPTH_LIMIT) { "Recursion depth limit $RECURSION_DEPTH_LIMIT exceeded" }
            listener.startedReduction(depth, expression)
            return when (expression) {
                is Application ->
                    when (val functionValue = reduce(expression.function, depth + 1, listener)) {
                        is Function -> {
                            val result = functionValue.body.replace(functionValue.name, expression.argument)
                            listener.reduced(depth, functionValue, expression.argument, result)
                            reduce(result, depth + 1, listener)
                        }
                        else -> application(functionValue, reduce(expression.argument, depth + 1, listener))
                    }
                else -> expression
            }
        }
    }
}

interface ReductionListener {

    fun startedReduction(depth: Int, expression: Expression)

    fun reduced(depth: Int, function: Expression, argument: Expression, result: Expression)
}
