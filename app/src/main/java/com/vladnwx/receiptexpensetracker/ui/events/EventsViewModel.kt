package com.vladnwx.receiptexpensetracker.ui.events

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vladnwx.receiptexpensetracker.data.local.dao.ContactDao
import com.vladnwx.receiptexpensetracker.data.local.dao.EventDao
import com.vladnwx.receiptexpensetracker.data.local.dao.EventParticipantDao
import com.vladnwx.receiptexpensetracker.data.local.dao.ExpenseDao
import com.vladnwx.receiptexpensetracker.data.local.entity.ContactEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.EventEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.EventParticipantEntity
import com.vladnwx.receiptexpensetracker.data.util.CalendarHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventWithDetails(
    val event: EventEntity,
    val participants: List<EventParticipantEntity>,
    val totalSpent: Double = 0.0
)

data class EventsUiState(
    val events: List<EventWithDetails> = emptyList(),
    val contacts: List<ContactEntity> = emptyList(),
    val showCreateDialog: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EventsViewModel @Inject constructor(
    application: Application,
    private val eventDao: EventDao,
    private val eventParticipantDao: EventParticipantDao,
    private val contactDao: ContactDao,
    private val expenseDao: ExpenseDao
) : AndroidViewModel(application) {

    private val context: Context get() = getApplication()

    private val _state = MutableStateFlow(EventsUiState())
    val state = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val contacts = contactDao.getAll()
            val events = eventDao.getAll()

            val withDetails = events.map { event ->
                val participants = eventParticipantDao.getByEventId(event.id)
                val expenses = expenseDao.getByEventId(event.id)
                val totalSpent = expenses.sumOf { it.amount }
                EventWithDetails(event = event, participants = participants, totalSpent = totalSpent)
            }

            _state.value = _state.value.copy(
                events = withDetails,
                contacts = contacts,
                loading = false
            )
        }
    }

    fun showCreateDialog() {
        _state.value = _state.value.copy(showCreateDialog = true)
    }

    fun hideCreateDialog() {
        _state.value = _state.value.copy(showCreateDialog = false)
    }

    fun createEvent(name: String, description: String?) {
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                eventDao.insert(EventEntity(name = name, description = description, date = now))

                if (CalendarHelper.hasCalendarPermission(context)) {
                    CalendarHelper.addEvent(
                        context = context,
                        title = name,
                        description = description,
                        eventDate = now
                    )
                }

                _state.value = _state.value.copy(showCreateDialog = false)
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun deleteEvent(id: Long) {
        viewModelScope.launch {
            eventDao.deleteById(id)
            load()
        }
    }

    fun addParticipant(eventId: Long, contactId: Long, contribution: Double) {
        viewModelScope.launch {
            eventParticipantDao.insert(EventParticipantEntity(
                eventId = eventId, contactId = contactId, contribution = contribution
            ))
            load()
        }
    }

    fun removeParticipant(eventId: Long) {
        viewModelScope.launch {
            eventParticipantDao.deleteByEventId(eventId)
            load()
        }
    }
}
