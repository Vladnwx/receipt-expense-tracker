package com.vladnwx.receiptexpensetracker.ui.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vladnwx.receiptexpensetracker.data.local.entity.AccountEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.CategoryEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.ExpenseEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.OperationType
import com.vladnwx.receiptexpensetracker.data.repository.AccountRepository
import com.vladnwx.receiptexpensetracker.data.repository.CategoryRepository
import com.vladnwx.receiptexpensetracker.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExpenseFormState(
    val amountText: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val selectedCategory: CategoryEntity? = null,
    val selectedAccount: AccountEntity? = null,
    val quantityText: String = "",
    val priceText: String = "",
    val description: String = "",
    val tags: List<String> = emptyList(),
    val isFamilyExpense: Boolean = false,
    val categories: List<CategoryEntity> = emptyList(),
    val childrenMap: Map<Long, List<CategoryEntity>> = emptyMap(),
    val accounts: List<AccountEntity> = emptyList(),
    val saved: Boolean = false
)

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExpenseFormState())
    val state = _state.asStateFlow()

    private val operationType: OperationType = OperationType.EXPENSE

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val categories = categoryRepository.getAll()
            val parents = categoryRepository.getParents()
            val childrenMap = mutableMapOf<Long, List<CategoryEntity>>()
            parents.forEach { p ->
                childrenMap[p.id] = categoryRepository.getChildren(p.id)
            }
            val accounts = accountRepository.getAll()
            _state.value = _state.value.copy(
                categories = categories,
                childrenMap = childrenMap,
                accounts = accounts,
                selectedAccount = accounts.firstOrNull()
            )
        }
    }

    fun onAmountChanged(text: String) {
        _state.value = _state.value.copy(amountText = text)
    }

    fun onDateChanged(millis: Long) {
        _state.value = _state.value.copy(dateMillis = millis)
    }

    fun onCategorySelected(category: CategoryEntity) {
        _state.value = _state.value.copy(
            selectedCategory = category,
            isFamilyExpense = category.isFamilyDefault
        )
    }

    fun onAccountSelected(account: AccountEntity) {
        _state.value = _state.value.copy(selectedAccount = account)
    }

    fun onQuantityChanged(text: String) {
        _state.value = _state.value.copy(quantityText = text)
    }

    fun onPriceChanged(text: String) {
        _state.value = _state.value.copy(priceText = text)
    }

    fun onDescriptionChanged(text: String) {
        _state.value = _state.value.copy(description = text)
    }

    fun onFamilyChanged(value: Boolean) {
        _state.value = _state.value.copy(isFamilyExpense = value)
    }

    fun save() {
        val s = _state.value
        val amount = parseAmount(s.amountText) ?: return
        val quantity = parseAmount(s.quantityText)
        val price = parseAmount(s.priceText)
        val effectiveAmount = if (quantity != null && price != null) quantity * price else amount

        viewModelScope.launch {
            expenseRepository.save(ExpenseEntity(
                type = operationType,
                categoryId = s.selectedCategory?.id,
                accountId = s.selectedAccount?.id,
                amount = effectiveAmount,
                quantity = quantity,
                price = price,
                description = s.description.ifBlank { null },
                tags = s.tags.joinToString(",").ifBlank { null },
                date = s.dateMillis,
                isFamilyExpense = s.isFamilyExpense
            ))
            _state.value = _state.value.copy(saved = true)
        }
    }

    fun resetForm() {
        val accounts = _state.value.accounts
        _state.value = ExpenseFormState(
            accounts = accounts,
            selectedAccount = accounts.firstOrNull()
        )
    }

    private fun parseAmount(text: String): Double? {
        val cleaned = text.replace(" ", "").replace(",", ".").trim()
        return cleaned.toDoubleOrNull()
    }
}
