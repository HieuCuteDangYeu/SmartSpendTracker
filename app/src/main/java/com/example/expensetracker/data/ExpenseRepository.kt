package com.example.expensetracker.data

import androidx.lifecycle.LiveData

class ExpenseRepository(private val dao: ExpenseDao) {
    val allExpenses: LiveData<List<Expense>> = dao.getAllExpenses()
    val totalAmount: LiveData<Double?> = dao.getTotalAmount()

    suspend fun insert(expense: Expense) = dao.insert(expense)
    suspend fun update(expense: Expense) = dao.update(expense)
    suspend fun delete(expense: Expense) = dao.delete(expense)
}
