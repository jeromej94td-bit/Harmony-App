package com.example.data.model

data class CoupleChoice(val userChoice: String, val partnerChoice: String) {
    val isMatch: Boolean get() = userChoice == partnerChoice
}

object EitherOrAnswerCodec {
    private const val PREFIX = "couple-choice-v1:"
    private const val SEPARATOR = "\u001F"

    fun encode(userChoice: String, partnerChoice: String): String =
        PREFIX + userChoice.replace(SEPARATOR, " ") + SEPARATOR + partnerChoice.replace(SEPARATOR, " ")

    fun decode(value: String): CoupleChoice? {
        if (!value.startsWith(PREFIX)) return null
        val parts = value.removePrefix(PREFIX).split(SEPARATOR, limit = 2)
        return if (parts.size == 2) CoupleChoice(parts[0], parts[1]) else null
    }
}
