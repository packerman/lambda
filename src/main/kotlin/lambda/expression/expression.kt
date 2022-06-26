package lambda.expression

sealed interface Expression {

    data class Name(val name: String): Expression {
        override fun toString() = name
    }

    data class Function(val name: String, val body: Expression): Expression {
        override fun toString() = "\\$name.$body"
    }

    data class Application(val function: Expression, val argument: Expression): Expression {
        override fun toString() = "($function $argument)"
    }
}
