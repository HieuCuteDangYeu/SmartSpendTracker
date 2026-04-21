package com.example.expensetracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.AppDatabase
import com.example.expensetracker.data.Expense
import com.example.expensetracker.data.ExpenseRepository
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ExpenseRepository
    val allExpenses: LiveData<List<Expense>>
    val totalAmount: LiveData<Double?>

    init {
        val dao = AppDatabase.getDatabase(application).expenseDao()
        repository = ExpenseRepository(dao)
        allExpenses = repository.allExpenses
        totalAmount = repository.totalAmount
    }

    fun insert(expense: Expense) = viewModelScope.launch { repository.insert(expense) }
    fun update(expense: Expense) = viewModelScope.launch { repository.update(expense) }
    fun delete(expense: Expense) = viewModelScope.launch { repository.delete(expense) }
}
