package lambda.expression

sealed interface Expression {

    private data class Name(val name: String) : Expression {
        override fun toString() = name
    }

    private data class Function(val name: String, val body: Expression) : Expression {
        override fun toString() = "\\$name.$body"
    }

    private data class Application(val function: Expression, val argument: Expression) : Expression {
        override fun toString() = "($function $argument)"
    }

    companion object {

        fun name(name: String): Expression = Name(name)

        fun function(name: String, body: Expression): Expression = Function(name, body)

        fun application(function: Expression, argument: Expression): Expression = Application(function, argument)
    }
}
