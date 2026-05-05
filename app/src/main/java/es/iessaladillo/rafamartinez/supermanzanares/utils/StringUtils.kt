package es.iessaladillo.rafamartinez.supermanzanares.utils

import java.text.Normalizer
import java.util.Locale

fun normalize(text: String): String {
    return Normalizer.normalize(text, Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "").lowercase()
}

fun formatName(fullName: String): String {
    return fullName.trim()
        .replace(Regex("\\s+"), " ")
        .lowercase()
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
}

fun formatPrice(price: Double?): String =
    if (price != null) "${String.format(Locale.forLanguageTag("es-ES"), "%.2f", price)}€" else ""

