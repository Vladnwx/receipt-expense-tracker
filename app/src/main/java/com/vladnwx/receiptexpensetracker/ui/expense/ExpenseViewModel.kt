package com.vladnwx.receiptexpensetracker.ui.expense

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vladnwx.receiptexpensetracker.data.local.entity.AccountEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.CategoryEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.ExpenseEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.OperationType
import com.vladnwx.receiptexpensetracker.data.repository.AccountRepository
import com.vladnwx.receiptexpensetracker.data.repository.CategoryRepository
import com.vladnwx.receiptexpensetracker.data.repository.ExpenseRepository
import com.vladnwx.receiptexpensetracker.data.reporter.AppLogger
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
    val attachmentPath: String? = null,
    val attachmentName: String? = null,
    val categories: List<CategoryEntity> = emptyList(),
    val childrenMap: Map<Long, List<CategoryEntity>> = emptyMap(),
    val accounts: List<AccountEntity> = emptyList(),
    val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    application: Application,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository
) : AndroidViewModel(application) {

    private val context: Context get() = getApplication()

    private val _state = MutableStateFlow(ExpenseFormState())
    val state = _state.asStateFlow()

    var operationType: OperationType = OperationType.EXPENSE
        private set

    fun configure(type: OperationType) {
        operationType = type
        loadData()
    }

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            AppLogger.d("ExpenseVM", "loadData: operationType=$operationType")
            val categories = categoryRepository.getAll()
            val parents = categoryRepository.getParents()
            val childrenMap = mutableMapOf<Long, List<CategoryEntity>>()
            parents.forEach { p ->
                childrenMap[p.id] = categoryRepository.getChildren(p.id)
            }
            val accounts = accountRepository.getAll()
            AppLogger.d("ExpenseVM", "loaded ${categories.size} categories, ${accounts.size} accounts")
            _state.value = _state.value.copy(
                categories = categories,
                childrenMap = childrenMap,
                accounts = accounts,
                selectedAccount = accounts.firstOrNull()
            )
        }
    }

    fun onAmountChanged(text: String) {
        val s = _state.value
        val amount = parseAmount(text)
        if (amount != null && amount > 0 && s.quantityText.isBlank() && s.priceText.isBlank()) {
            _state.value = s.copy(
                amountText = text,
                quantityText = "1",
                priceText = text,
                error = null
            )
        } else {
            _state.value = s.copy(amountText = text, error = null)
        }
    }

    fun onDateChanged(millis: Long) {
        _state.value = _state.value.copy(dateMillis = millis, error = null)
    }

    fun onCategorySelected(category: CategoryEntity) {
        _state.value = _state.value.copy(
            selectedCategory = category,
            isFamilyExpense = category.isFamilyDefault,
            error = null
        )
    }

    fun onAccountSelected(account: AccountEntity) {
        _state.value = _state.value.copy(selectedAccount = account)
    }

    fun saveCategory(name: String, parentId: Long? = null) {
        viewModelScope.launch {
            categoryRepository.save(CategoryEntity(
                name = name,
                parentId = parentId,
                isPredefined = false
            ))
            loadData()
        }
    }

    fun onQuantityChanged(text: String) {
        _state.value = _state.value.copy(quantityText = text, error = null)
    }

    fun onPriceChanged(text: String) {
        _state.value = _state.value.copy(priceText = text, error = null)
    }

    fun onDescriptionChanged(text: String) {
        _state.value = _state.value.copy(description = text, error = null)
        if (text.length >= 3 && _state.value.selectedCategory == null) {
            suggestFromHistory(text)
        }
    }

    private fun suggestFromHistory(desc: String) {
        viewModelScope.launch {
            val matches = expenseRepository.search(desc)
            if (matches.isNotEmpty()) {
                val best = matches.first()
                val cat = best.categoryId?.let { id ->
                    _state.value.categories.find { it.id == id }
                }
                val acc = best.accountId?.let { id ->
                    _state.value.accounts.find { it.id == id }
                }
                if (cat != null && _state.value.selectedCategory == null) {
                    _state.value = _state.value.copy(
                        selectedCategory = cat,
                        isFamilyExpense = cat.isFamilyDefault
                    )
                }
                if (acc != null && _state.value.selectedAccount == null) {
                    _state.value = _state.value.copy(selectedAccount = acc)
                }
            }
        }
    }

    fun removeTag(tag: String) {
        _state.value = _state.value.copy(
            tags = _state.value.tags - tag
        )
    }

    fun addTag(tag: String) {
        if (tag.isBlank() || _state.value.tags.contains(tag)) return
        _state.value = _state.value.copy(
            tags = _state.value.tags + tag
        )
    }

    fun onFamilyChanged(value: Boolean) {
        _state.value = _state.value.copy(isFamilyExpense = value)
    }

    fun setAttachment(path: String, name: String) {
        _state.value = _state.value.copy(attachmentPath = path, attachmentName = name)
    }

    fun clearAttachment() {
        _state.value = _state.value.copy(attachmentPath = null, attachmentName = null)
    }

    fun save() {
        val s = _state.value
        val amount = parseAmount(s.amountText)
        if (amount == null || amount <= 0) {
            AppLogger.w("ExpenseVM", "save: amount invalid: ${s.amountText}")
            _state.value = s.copy(error = "Укажите сумму больше 0")
            return
        }
        if (s.selectedCategory == null) {
            AppLogger.w("ExpenseVM", "save: category not selected")
            _state.value = s.copy(error = "Выберите категорию")
            return
        }
        val quantity = parseAmount(s.quantityText)
        val price = parseAmount(s.priceText)
        val (finalQuantity, finalPrice, effectiveAmount) = if (quantity != null && price != null) {
            Triple(quantity, price, quantity * price)
        } else {
            Triple(1.0, amount, amount)
        }

        viewModelScope.launch {
            AppLogger.d("ExpenseVM", "save: type=$operationType amount=$effectiveAmount cat=${s.selectedCategory.name} acc=${s.selectedAccount?.name}")
            expenseRepository.save(ExpenseEntity(
                type = operationType,
                categoryId = s.selectedCategory.id,
                accountId = s.selectedAccount?.id,
                amount = effectiveAmount,
                quantity = finalQuantity,
                price = finalPrice,
                description = s.description.ifBlank { null },
                tags = s.tags.joinToString(",").ifBlank { null },
                date = s.dateMillis,
                isFamilyExpense = s.isFamilyExpense,
                attachmentPath = s.attachmentPath
            ))
            updateWidget()
            _state.value = _state.value.copy(saved = true)
            AppLogger.d("ExpenseVM", "save: success")
        }
    }

    private suspend fun updateWidget() {
        try {
            val total = expenseRepository.getTotal()
            com.vladnwx.receiptexpensetracker.WidgetUpdater.updateBalance(context, total)
        } catch (_: Exception) { }
    }

    fun resetForm() {
        val accounts = _state.value.accounts
        val categories = _state.value.categories
        val childrenMap = _state.value.childrenMap
        _state.value = ExpenseFormState(
            accounts = accounts,
            categories = categories,
            childrenMap = childrenMap,
            selectedAccount = accounts.firstOrNull()
        )
    }

    private fun parseAmount(text: String): Double? {
        val cleaned = text.replace(" ", "").replace(",", ".").trim()
        return cleaned.toDoubleOrNull()
    }
}
