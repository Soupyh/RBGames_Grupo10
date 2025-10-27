package com.example.rbgames_grupo1.ui.viewmodel

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rbgames_grupo1.data.repository.UserRepository
import com.example.rbgames_grupo1.domain.validation.validateConfirm
import com.example.rbgames_grupo1.domain.validation.validateEmail
import com.example.rbgames_grupo1.domain.validation.validateNameLettersOnly
import com.example.rbgames_grupo1.domain.validation.validatePhoneDigitsOnly
import com.example.rbgames_grupo1.domain.validation.validateStrongPassword

// ----------------- ESTADOS DE UI (observable con StateFlow) -----------------

data class LoginUiState(                                   // Estado de la pantalla Login
    val email: String = "",                                // Campo email
    val pass: String = "",                                 // Campo contraseña (texto)
    val emailError: String? = null,                        // Error de email
    val passError: String? = null,                         // (Opcional) error de pass en login
    val isSubmitting: Boolean = false,                     // Flag de carga
    val canSubmit: Boolean = false,                        // Habilitar botón
    val success: Boolean = false,                          // Resultado OK
    val errorMsg: String? = null                           // Error global (credenciales inválidas)
)

data class RegisterUiState(                                // Estado de la pantalla Registro (<= 5 campos)
    val name: String = "",                                 // 1) Nombre
    val email: String = "",                                // 2) Email
    val phone: String = "",                                // 3) Teléfono
    val pass: String = "",                                 // 4) Contraseña
    val confirm: String = "",                              // 5) Confirmación

    val nameError: String? = null,                         // Errores por campo
    val emailError: String? = null,
    val phoneError: String? = null,
    val passError: String? = null,
    val confirmError: String? = null,

    val isSubmitting: Boolean = false,                     // Flag de carga
    val canSubmit: Boolean = false,                        // Habilitar botón
    val success: Boolean = false,                          // Resultado OK
    val errorMsg: String? = null                           // Error global (ej: duplicado)
)

// ----------------- SESIÓN Y PERFIL -----------------

data class UserProfile(
    val nombre: String = "",
    val email: String = "",
    val telefono: String = ""
)

data class SessionState(
    val isLoggedIn: Boolean = false,
    val user: UserProfile? = null
)

// ----------------- COLECCIÓN EN MEMORIA (solo para la demo) -----------------

// Modelo mínimo de usuario para la colección (no se expone)
private data class DemoUser(
    val name: String,
    val email: String,
    val phone: String,
    val pass: String
)

class AuthViewModel(
    private val repository: UserRepository
) : ViewModel() { // ViewModel que maneja Login/Registro + Sesión/Perfil

    // --- Flujos de estado para observar desde la UI ---
    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register

    // --- Sesión/Perfil (nuevo) ---
    private val _session = MutableStateFlow(SessionState())
    val session: StateFlow<SessionState> = _session

    // ----------------- API de sesión/perfil (nuevo) -----------------

    /** Marca sesión iniciada con un perfil. */
    fun setLoggedIn(profile: UserProfile) {
        _session.value = SessionState(isLoggedIn = true, user = profile)
    }

    /** Actualiza campos del perfil en memoria (y aquí puedes disparar update remoto si tienes backend). */
    fun updateProfile(nombre: String, email: String, telefono: String) {
        _session.update { s ->
            s.copy(user = s.user?.copy(nombre = nombre, email = email, telefono = telefono))
        }
        // TODO: Si tienes backend, llamar aquí al repositorio para persistir cambios.
    }

    /** Cierra sesión y limpia el perfil. */
    fun logout() {
        _session.value = SessionState(isLoggedIn = false, user = null)
    }

    // ----------------- LOGIN: handlers y envío -----------------

    fun onLoginEmailChange(value: String) {
        _login.update { it.copy(email = value, emailError = validateEmail(value)) }
        recomputeLoginCanSubmit()
    }

    fun onLoginPassChange(value: String) {
        _login.update { it.copy(pass = value) }
        recomputeLoginCanSubmit()
    }

    private fun recomputeLoginCanSubmit() {
        val s = _login.value
        val can = s.emailError == null && s.email.isNotBlank() && s.pass.isNotBlank()
        _login.update { it.copy(canSubmit = can) }
    }

    fun submitLogin() {
        val s = _login.value
        if (!s.canSubmit || s.isSubmitting) return
        viewModelScope.launch {
            _login.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }
            delay(500) // Simula latencia de red

            // Consulta real a la BD vía repositorio
            val result = repository.login(s.email.trim(), s.pass)

            _login.update {
                if (result.isSuccess) {
                    // --- NUEVO: marca sesión iniciada con perfil básico ---
                    // Si tienes un endpoint para obtener el perfil, úsalo aquí.
                    // Por ahora tomamos el email del login y dejamos nombre/teléfono vacíos
                    // para que el usuario pueda editarlos en AccountScreen.
                    setLoggedIn(
                        UserProfile(
                            nombre = "",
                            email = s.email.trim(),
                            telefono = ""
                        )
                    )
                    it.copy(isSubmitting = false, success = true, errorMsg = null)
                } else {
                    it.copy(
                        isSubmitting = false,
                        success = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "Error de autenticación"
                    )
                }
            }
        }
    }

    fun clearLoginResult() {
        _login.update { it.copy(success = false, errorMsg = null) }
    }

    // ----------------- REGISTRO: handlers y envío -----------------

    fun onNameChange(value: String) {
        val filtered = value.filter { it.isLetter() || it.isWhitespace() }
        _register.update { it.copy(name = filtered, nameError = validateNameLettersOnly(filtered)) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterEmailChange(value: String) {
        _register.update { it.copy(email = value, emailError = validateEmail(value)) }
        recomputeRegisterCanSubmit()
    }

    fun onPhoneChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        _register.update { it.copy(phone = digitsOnly, phoneError = validatePhoneDigitsOnly(digitsOnly)) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterPassChange(value: String) {
        _register.update { it.copy(pass = value, passError = validateStrongPassword(value)) }
        _register.update { it.copy(confirmError = validateConfirm(it.pass, it.confirm)) } // Revalidar confirmación
        recomputeRegisterCanSubmit()
    }

    fun onConfirmChange(value: String) {
        _register.update { it.copy(confirm = value, confirmError = validateConfirm(it.pass, value)) }
        recomputeRegisterCanSubmit()
    }

    private fun recomputeRegisterCanSubmit() {
        val s = _register.value
        val noErrors = listOf(s.nameError, s.emailError, s.phoneError, s.passError, s.confirmError).all { it == null }
        val filled = s.name.isNotBlank() && s.email.isNotBlank() && s.phone.isNotBlank() && s.pass.isNotBlank() && s.confirm.isNotBlank()
        _register.update { it.copy(canSubmit = noErrors && filled) }
    }

    fun submitRegister() {
        val s = _register.value
        if (!s.canSubmit || s.isSubmitting) return
        viewModelScope.launch {
            _register.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }
            delay(700) // Simula latencia de red

            // Inserta en BD vía repositorio
            val result = repository.register(
                name = s.name.trim(),
                email = s.email.trim(),
                phone = s.phone.trim(),
                password = s.pass
            )

            _register.update {
                if (result.isSuccess) {
                    // Nota: no auto-logueamos tras registrar (puedes hacerlo si lo deseas).
                    it.copy(isSubmitting = false, success = true, errorMsg = null)
                } else {
                    it.copy(
                        isSubmitting = false,
                        success = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "No se pudo registrar"
                    )
                }
            }
        }
    }

    fun clearRegisterResult() {
        _register.update { it.copy(success = false, errorMsg = null) }
    }
}
