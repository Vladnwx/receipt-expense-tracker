package com.qrcode.scanner.ui.expenses

import com.qrcode.scanner.data.local.entity.CategoryEntity
import com.qrcode.scanner.data.local.entity.ExpenseEntity
import com.qrcode.scanner.data.repository.CategoryRepository
import com.qrcode.scanner.data.repository.ExpenseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExpensesViewModelTest {

    private val expenseRepository: ExpenseRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()
    private lateinit var viewModel: ExpensesViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has ALL date filter and loading`() = runTest {
        coEvery { categoryRepository.getAll() } returns emptyList()
        coEvery { expenseRepository.getByDateRange(any(), any()) } returns emptyList()

        viewModel = ExpensesViewModel(expenseRepository, categoryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(DateFilter.ALL, viewModel.uiState.value.dateFilter)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `setDateFilter updates date filter in state`() = runTest {
        coEvery { categoryRepository.getAll() } returns emptyList()
        coEvery { expenseRepository.getByDateRange(any(), any()) } returns emptyList()

        viewModel = ExpensesViewModel(expenseRepository, categoryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setDateFilter(DateFilter.TODAY)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(DateFilter.TODAY, viewModel.uiState.value.dateFilter)
    }

    @Test
    fun `setDateFilter to WEEK calls repository`() = runTest {
        coEvery { categoryRepository.getAll() } returns emptyList()
        coEvery { expenseRepository.getByDateRange(any(), any()) } returns emptyList()

        viewModel = ExpensesViewModel(expenseRepository, categoryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setDateFilter(DateFilter.WEEK)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { expenseRepository.getByDateRange(any(), any()) }
    }

    @Test
    fun `setCategoryFilter updates selected category in state`() = runTest {
        coEvery { categoryRepository.getAll() } returns emptyList()
        coEvery { expenseRepository.getByDateRange(any(), any()) } returns emptyList()
        coEvery { expenseRepository.getByCategoryId(any()) } returns emptyList()

        viewModel = ExpensesViewModel(expenseRepository, categoryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setCategoryFilter(42L)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(42L, viewModel.uiState.value.selectedCategoryId)
    }

    @Test
    fun `setCategoryFilter to null clears filter`() = runTest {
        coEvery { categoryRepository.getAll() } returns emptyList()
        coEvery { expenseRepository.getByDateRange(any(), any()) } returns emptyList()
        coEvery { expenseRepository.getByCategoryId(any()) } returns emptyList()

        viewModel = ExpensesViewModel(expenseRepository, categoryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setCategoryFilter(null)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.selectedCategoryId)
    }

    @Test
    fun `expenses total is sum of amounts`() = runTest {
        val expenses = listOf(
            ExpenseEntity(id = 1, amount = 100.0, date = 1000L, categoryId = 1),
            ExpenseEntity(id = 2, amount = 200.0, date = 1001L, categoryId = 1),
            ExpenseEntity(id = 3, amount = 300.0, date = 1002L, categoryId = 1)
        )

        coEvery { categoryRepository.getAll() } returns emptyList()
        coEvery { expenseRepository.getByDateRange(any(), any()) } returns expenses

        viewModel = ExpensesViewModel(expenseRepository, categoryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(600.0, viewModel.uiState.value.total, 0.001)
        assertEquals(3, viewModel.uiState.value.expenses.size)
    }

    @Test
    fun `categories are loaded from repository`() = runTest {
        val categories = listOf(
            CategoryEntity(id = 1, name = "Food"),
            CategoryEntity(id = 2, name = "Transport")
        )

        coEvery { categoryRepository.getAll() } returns categories
        coEvery { expenseRepository.getByDateRange(any(), any()) } returns emptyList()

        viewModel = ExpensesViewModel(expenseRepository, categoryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.categories.size)
        assertEquals("Food", viewModel.uiState.value.categories[0].name)
    }
}
