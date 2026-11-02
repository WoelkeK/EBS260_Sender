package pl.woelke.printersender.exceptions

class CustomConnectionException(
    val code: String,
    message: String
) : Exception(message) {
    /**
     * Handle custom exception, not implemented yet
     */
    override fun toString(): String {
        return "Błąd ($code): $message"
    }
}