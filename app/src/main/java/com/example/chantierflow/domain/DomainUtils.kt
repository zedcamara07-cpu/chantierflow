package com.example.chantierflow.domain

import com.example.chantierflow.model.TaskEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DomainUtils {

    fun toCents(input: String): Long {
        val cleaned = input.trim().replace(",", ".")
        val regex = Regex("^\\d{1,8}(\\.\\d{1,2})?$")
        if (!regex.matches(cleaned)) {
            throw IllegalArgumentException("Saisissez un montant positif avec au maximum deux décimales.")
        }
        val parts = cleaned.split(".")
        val whole = parts[0].toLong()
        val fraction = if (parts.size > 1) parts[1].padEnd(2, '0').take(2).toLong() else 0L
        val cents = whole * 100 + fraction
        if (cents <= 0) {
            throw IllegalArgumentException("Le montant doit être supérieur à zéro.")
        }
        return cents
    }

    fun formatMoney(amountCents: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale.FRANCE)
        return format.format(amountCents / 100.0)
    }

    fun calculateProgress(tasks: List<TaskEntity>): Int? {
        if (tasks.isEmpty()) return null
        val doneCount = tasks.count { it.status.lowercase() == "done" }
        return Math.round((doneCount.toFloat() / tasks.size.toFloat()) * 100f)
    }

    fun localToday(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE)
        return formatter.format(Date())
    }

    fun formatDate(value: String?): String {
        if (value.isNullOrBlank()) return "Non renseignée"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE)
            val date = inputFormat.parse(value) ?: return value
            val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale.FRANCE)
            outputFormat.format(date)
        } catch (_: Exception) {
            value
        }
    }
}
