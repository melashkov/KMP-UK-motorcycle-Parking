package com.melashkov.mcparking.ui.support

data class FeedbackValidation(
    val messageLength: Int,
    val messageValid: Boolean,
    val emailValid: Boolean,
) {
    val valid: Boolean get() = messageValid && emailValid
    val messageTooLong: Boolean get() = messageLength > FeedbackValidator.MAX_MESSAGE_LENGTH
}

object FeedbackValidator {
    const val MIN_MESSAGE_LENGTH = 5
    const val MAX_MESSAGE_LENGTH = 1500
    private const val MAX_EMAIL_LENGTH = 254
    private val emailPattern = Regex("""[^\s@]+@[^\s@]+\.[^\s@]+""")

    fun validate(message: String, email: String): FeedbackValidation {
        val messageLength = message.trim().length
        val normalizedEmail = email.trim()
        return FeedbackValidation(
            messageLength = messageLength,
            messageValid = messageLength in MIN_MESSAGE_LENGTH..MAX_MESSAGE_LENGTH &&
                message.none { it.code in 0..8 || it.code in 11..12 || it.code in 14..31 || it.code == 127 },
            emailValid = normalizedEmail.isEmpty() ||
                (normalizedEmail.length <= MAX_EMAIL_LENGTH && emailPattern.matches(normalizedEmail)),
        )
    }
}
