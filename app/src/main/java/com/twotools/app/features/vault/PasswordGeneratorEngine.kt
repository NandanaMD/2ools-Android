package com.twotools.app.features.vault

import java.security.SecureRandom
import kotlin.math.log2

object PasswordGeneratorEngine {

    private val random = SecureRandom()

    private const val UPPERCASE = "ABCDEFGHJKLMNPQRSTUVWXYZ" // Omitted confusing 'I', 'O'
    private const val LOWERCASE = "abcdefghijkmnpqrstuvwxyz" // Omitted confusing 'l', 'o'
    private const val DIGITS = "23456789"                   // Omitted '0', '1'
    private const val SYMBOLS = "!@#$%^&*()-_=+[]{}<>?"

    fun generatePassword(
        length: Int = 16,
        includeUpper: Boolean = true,
        includeLower: Boolean = true,
        includeDigits: Boolean = true,
        includeSymbols: Boolean = true
    ): String {
        val pool = StringBuilder()
        val guaranteed = mutableListOf<Char>()

        if (includeUpper) {
            pool.append(UPPERCASE)
            guaranteed.add(UPPERCASE[random.nextInt(UPPERCASE.length)])
        }
        if (includeLower) {
            pool.append(LOWERCASE)
            guaranteed.add(LOWERCASE[random.nextInt(LOWERCASE.length)])
        }
        if (includeDigits) {
            pool.append(DIGITS)
            guaranteed.add(DIGITS[random.nextInt(DIGITS.length)])
        }
        if (includeSymbols) {
            pool.append(SYMBOLS)
            guaranteed.add(SYMBOLS[random.nextInt(SYMBOLS.length)])
        }

        if (pool.isEmpty()) return ""

        val chars = pool.toString()
        val passwordChars = ArrayList<Char>(length)
        passwordChars.addAll(guaranteed)

        for (i in passwordChars.size until length) {
            passwordChars.add(chars[random.nextInt(chars.length)])
        }

        // Shuffle guaranteed characters
        passwordChars.shuffle(random)
        return passwordChars.joinToString("")
    }

    fun calculateStrength(password: String): Pair<String, Float> {
        if (password.isEmpty()) return "Empty" to 0f

        var poolSize = 0
        if (password.any { it.isUpperCase() }) poolSize += 26
        if (password.any { it.isLowerCase() }) poolSize += 26
        if (password.any { it.isDigit() }) poolSize += 10
        if (password.any { !it.isLetterOrDigit() }) poolSize += 32

        if (poolSize == 0) return "Very Weak" to 0.1f

        val entropy = password.length * log2(poolSize.toDouble())

        return when {
            entropy < 40 -> "Weak" to 0.25f
            entropy < 65 -> "Moderate" to 0.50f
            entropy < 90 -> "Strong" to 0.75f
            else -> "Unbreakable" to 1.0f
        }
    }
}
