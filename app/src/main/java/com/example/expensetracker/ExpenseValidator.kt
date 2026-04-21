package com.example.expensetracker

import com.example.expensetracker.data.Expense
import java.text.SimpleDateFormat
import java.util.Locale

sealed class ValidationResult {
    data class Valid(val name: String, val amount: Double, val date: String, val category: String) : ValidationResult()
    data class Invalid(val errors: Map<String, Int>) : ValidationResult()
}

object ExpenseValidator {
    fun validate(name: String, amountStr: String, dateStr: String, category: String): ValidationResult {
        val errors = mutableMapOf<String, Int>()

        if (name.isBlank()) errors["name"] = R.string.error_name_empty

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) errors["amount"] = R.string.error_invalid_amount

        if (!isValidDate(dateStr)) errors["date"] = R.string.error_invalid_date

        if (category.isBlank()) errors["category"] = R.string.error_no_category

        return if (errors.isEmpty()) {
            ValidationResult.Valid(name.trim(), amount!!, dateStr, category)
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    private fun isValidDate(dateStr: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdf.isLenient = false
            sdf.parse(dateStr) != null
        } catch (e: Exception) {
            false
        }
    }
}
