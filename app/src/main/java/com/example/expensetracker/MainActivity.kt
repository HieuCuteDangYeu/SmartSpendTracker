package com.example.expensetracker

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensetracker.data.Expense
import com.example.expensetracker.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ExpenseViewModel by viewModels()
    private lateinit var adapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val paddingLeft = binding.root.paddingLeft
        val paddingTop = binding.root.paddingTop
        val paddingRight = binding.root.paddingRight
        val paddingBottom = binding.root.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                paddingLeft + systemBars.left,
                paddingTop + systemBars.top,
                paddingRight + systemBars.right,
                paddingBottom + systemBars.bottom
            )
            insets
        }

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(
            onEditClick = { expense -> showAddEditDialog(expense) },
            onDeleteClick = { expense -> showDeleteConfirmation(expense) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupListeners() {
        binding.fab.setOnClickListener {
            showAddEditDialog(null)
        }
    }

    private fun observeViewModel() {
        viewModel.allExpenses.observe(this) { expenses ->
            adapter.submitList(expenses)
            binding.emptyStateText.visibility = if (expenses.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.totalAmount.observe(this) { total ->
            val amount = total ?: 0.0
            binding.totalAmountText.text = getString(R.string.total_expenses_format, String.format("%.2f", amount))
        }
    }

    private fun showAddEditDialog(expense: Expense?) {
        val dialog = AddEditExpenseDialog.newInstance(expense)
        dialog.show(supportFragmentManager, AddEditExpenseDialog.TAG)
    }

    private fun showDeleteConfirmation(expense: Expense) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_title)
            .setMessage(R.string.delete_message)
            .setPositiveButton(R.string.action_confirm_delete) { _, _ ->
                viewModel.delete(expense)
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }
}
