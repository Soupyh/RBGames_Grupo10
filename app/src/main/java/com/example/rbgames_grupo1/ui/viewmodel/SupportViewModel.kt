package com.example.rbgames_grupo1.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rbgames_grupo1.data.local.reports.ReportEntity
import com.example.rbgames_grupo1.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ReportFormUi(
    val subject: String = "",
    val message: String = "",
    val sending: Boolean = false,
    val error: String? = null,
    val successId: Long? = null
)

class SupportViewModel(
    private val repo: ReportRepository
) : ViewModel() {

    private val _form = MutableStateFlow(ReportFormUi())
    val form: StateFlow<ReportFormUi> = _form

    private val _inbox = MutableStateFlow<List<ReportEntity>>(emptyList())
    val inbox: StateFlow<List<ReportEntity>> = _inbox

    fun setSubject(v: String) { _form.value = _form.value.copy(subject = v, error = null, successId = null) }
    fun setMessage(v: String) { _form.value = _form.value.copy(message = v, error = null, successId = null) }

    fun send(email: String) {
        val f = _form.value
        if (f.subject.isBlank() || f.message.isBlank()) {
            _form.value = f.copy(error = "Completa asunto y mensaje")
            return
        }
        viewModelScope.launch {
            _form.value = f.copy(sending = true, error = null, successId = null)
            val res = repo.submit(email, f.subject, f.message)
            _form.value = res.fold(
                onSuccess = { id -> ReportFormUi(successId = id) },
                onFailure = { e -> f.copy(sending = false, error = e.message ?: "Error al enviar") }
            )
        }
    }

    fun loadInbox(role: Role, myEmail: String) {
        viewModelScope.launch {
            _inbox.value = when (role) {
                Role.ADMIN, Role.SOPORTE -> repo.listAll()
                else -> repo.listMine(myEmail)
            }
        }
    }

    fun changeStatus(id: Long, status: String) {
        viewModelScope.launch {
            repo.setStatus(id, status)
            _inbox.value = _inbox.value.map { if (it.id == id) it.copy(status = status) else it }
        }
    }
}
