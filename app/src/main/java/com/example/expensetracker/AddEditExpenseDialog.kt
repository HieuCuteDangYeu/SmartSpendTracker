package com.example.expensetracker

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.expensetracker.data.Expense
import com.example.expensetracker.databinding.DialogAddEditExpenseBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddEditExpenseDialog : DialogFragment() {

    private var _binding: DialogAddEditExpenseBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExpenseViewModel by activityViewModels()

    private var expenseToEdit: Expense? = null

    companion object {
        const val TAG = "AddEditExpenseDialog"
        private const val ARG_EXPENSE = "arg_expense"

        fun newInstance(expense: Expense? = null): AddEditExpenseDialog {
            val args = Bundle().apply {
                putSerializable(ARG_EXPENSE, expense)
            }
            val fragment = AddEditExpenseDialog()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        expenseToEdit = arguments?.getSerializable(ARG_EXPENSE) as? Expense
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddEditExpenseBinding.inflate(layoutInflater)
        
        val titleRes = if (expenseToEdit == null) R.string.dialog_add_title else R.string.dialog_edit_title

        setupViews()
        populateData()
        setupListeners()

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(titleRes)
            .setView(binding.root)
            .setPositiveButton(R.string.action_save, null) // Set to null to prevent auto-dismissal
            .setNegativeButton(R.string.action_cancel) { _, _ -> dismiss() }
            .create().apply {
                setOnShowListener {
                    getButton(Dialog.BUTTON_POSITIVE).setOnClickListener {
                        saveExpense()
                    }
                }
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Return null because we are using onCreateDialog with MaterialAlertDialogBuilder
        return null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupViews() {
        val categories = resources.getStringArray(R.array.expense_categories)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, categories)
        binding.categoryInput.setAdapter(adapter)

        binding.dateInput.setOnClickListener {
            showDatePicker()
        }
    }

    private fun populateData() {
        expenseToEdit?.let { expense ->
            binding.nameInput.setText(expense.name)
            binding.amountInput.setText(expense.amount.toString())
            binding.dateInput.setText(expense.date)
            binding.categoryInput.setText(expense.category, false)
        } ?: run {
            // Default to today
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            binding.dateInput.setText(today)
        }
    }

    private fun setupListeners() {
        binding.nameInput.doOnTextChanged { _, _, _, _ -> binding.nameLayout.error = null }
        binding.amountInput.doOnTextChanged { _, _, _, _ -> binding.amountLayout.error = null }
        binding.dateInput.doOnTextChanged { _, _, _, _ -> binding.dateLayout.error = null }
        binding.categoryInput.doOnTextChanged { _, _, _, _ -> binding.categoryLayout.error = null }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            binding.dateInput.setText(sdf.format(Date(selection)))
        }

        datePicker.show(childFragmentManager, "DATE_PICKER")
    }

    private fun saveExpense() {
        val name = binding.nameInput.text.toString()
        val amountStr = binding.amountInput.text.toString()
        val dateStr = binding.dateInput.text.toString()
        val category = binding.categoryInput.text.toString()

        when (val result = ExpenseValidator.validate(name, amountStr, dateStr, category)) {
            is ValidationResult.Invalid -> {
                binding.nameLayout.error = result.errors["name"]?.let { getString(it) }
                binding.amountLayout.error = result.errors["amount"]?.let { getString(it) }
                binding.dateLayout.error = result.errors["date"]?.let { getString(it) }
                binding.categoryLayout.error = result.errors["category"]?.let { getString(it) }
            }
            is ValidationResult.Valid -> {
                val newExpense = Expense(
                    id = expenseToEdit?.id ?: 0,
                    name = result.name,
                    amount = result.amount,
                    date = result.date,
                    category = result.category
                )

                if (expenseToEdit == null) {
                    viewModel.insert(newExpense)
                } else {
                    viewModel.update(newExpense)
                }
                dismiss()
            }
        }
    }
}
