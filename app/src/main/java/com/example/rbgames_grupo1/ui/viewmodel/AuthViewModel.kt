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

// ----------------- ROLES -----------------
enum class Role { ADMIN, SOPORTE, USUARIO }

// ----------------- ESTADOS DE UI (observable con StateFlow) -----------------

data class LoginUiState(
    val email: String = "",
    val pass: String = "",
    val emailError: String? = null,
    val passError: String? = null,
    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null
)

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val pass: String = "",
    val confirm: String = "",

    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passError: String? = null,
    val confirmError: String? = null,

    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null
)

// ----------------- SESIÓN Y PERFIL -----------------

data class UserProfile(
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val role: Role = Role.USUARIO,       // ⬅️ ahora guardamos el rol
    val photoUri: String? = null
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
) : ViewModel() { // Maneja Login/Registro + Sesión/Perfil/Roles

    // --- Flujos de estado para observar desde la UI ---
    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register

    // --- Sesión/Perfil ---
    private val _session = MutableStateFlow(SessionState())
    val session: StateFlow<SessionState> = _session

    // ----------------- Helpers de Roles -----------------

    /** Convierte un string del repositorio al enum Role de forma segura. */
    private fun parseRole(text: String?): Role = when (text?.uppercase()) {
        "ADMIN"   -> Role.ADMIN
        "SOPORTE" -> Role.SOPORTE
        else      -> Role.USUARIO
    }

    /** Heurística de respaldo si el repo aún no expone rol. */
    private fun fallbackRoleByEmail(email: String): Role {
        val e = email.lowercase()
        return when {
            "admin" in e   -> Role.ADMIN
            "soporte" in e -> Role.SOPORTE
            else           -> Role.USUARIO
        }
    }

    // ----------------- API de sesión/perfil -----------------

    /** Marca sesión iniciada con un perfil. */
    fun setLoggedIn(profile: UserProfile) {
        _session.value = SessionState(isLoggedIn = true, user = profile)
    }

    /** Actualiza campos del perfil en memoria. (Persistir en repo si aplica) */
    fun updateProfile(nombre: String, email: String, telefono: String) {
        _session.update { s ->
            s.copy(user = s.user?.copy(nombre = nombre, email = email, telefono = telefono))
        }
        // TODO: repository.updateProfile(...) si tienes backend/BD local
    }

    /** Actualiza SOLO el rol del perfil (útil para pruebas o pantalla de roles). */
    fun updateRole(role: Role) {
        _session.update { s ->
            s.copy(user = s.user?.copy(role = role))
        }
        // TODO: repository.updateUserRole(...) si corresponde
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

            // 1) Validar credenciales
            val result = repository.login(s.email.trim(), s.pass)

            _login.update {
                if (result.isSuccess) {
                    // 2) 🔽 Cargar perfil completo desde Room y abrir sesión
                    val u = repository.findByEmail(s.email.trim())

                    setLoggedIn(
                        UserProfile(
                            nombre   = u?.name.orEmpty(),
                            email    = u?.email.orEmpty(),
                            telefono = u?.phone.orEmpty(),
                            role     = parseRole(u?.role),
                            photoUri = u?.photoUri
                        )
                    )

                    // 3) Marcar éxito del login para que la UI navegue
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
                // Si tu repo soporta rol en el registro, puedes pasar Role.USUARIO aquí.
            )

            _register.update { old ->
                if (result.isSuccess) {
                    // Nota: no auto-logueamos tras registrar (puedes hacerlo si lo deseas).
                    old.copy(isSubmitting = false, success = true, errorMsg = null)
                } else {
                    old.copy(
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

    fun updatePhoto(uriString: String?) {
        _session.update { s ->
            s.copy(user = s.user?.copy(photoUri = uriString))
        }
        // TODO: si persistes en Room, llama a repository.updatePhoto(email, uriString)
    }
}
