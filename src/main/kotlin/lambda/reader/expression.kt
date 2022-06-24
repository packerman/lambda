package lambda.reader

sealed interface Expression {

    data class Name(val name: String): Expression

    data class Function(val name: String, val body: Expression): Expression

    data class Application(val function: Expression, val argument: Expression): Expression
}
