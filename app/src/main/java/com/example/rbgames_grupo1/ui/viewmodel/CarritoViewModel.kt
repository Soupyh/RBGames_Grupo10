package com.example.rbgames_grupo1.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.text.NumberFormat
import java.util.Locale

data class CartItem(
    val id: String,
    val nombre: String,
    val precio: Int,          // CLP en enteros
    val imagenRes: Int?,      // opcional si usas drawables
    val cantidad: Int
) {
    val subtotal: Int get() = precio * cantidad
}

class CarritoViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items

    val total = items.map { list -> list.sumOf { it.subtotal } }

    fun addOrIncrement(item: CartItem) {
        _items.update { current ->
            val idx = current.indexOfFirst { it.id == item.id }
            if (idx >= 0) {
                current.toMutableList().also {
                    val existente = it[idx]
                    it[idx] = existente.copy(cantidad = existente.cantidad + item.cantidad.coerceAtLeast(1))
                }
            } else {
                current + item.copy(cantidad = item.cantidad.coerceAtLeast(1))
            }
        }
    }

    fun increment(id: String) {
        _items.update { current ->
            current.map { if (it.id == id) it.copy(cantidad = it.cantidad + 1) else it }
        }
    }

    fun decrement(id: String) {
        _items.update { current ->
            current.mapNotNull { item ->
                if (item.id != id) return@mapNotNull item
                val nueva = item.cantidad - 1
                if (nueva <= 0) null else item.copy(cantidad = nueva)
            }
        }
    }

    fun remove(id: String) {
        _items.update { current -> current.filterNot { it.id == id } }
    }

    fun clear() {
        _items.value = emptyList()
    }

    // Útil si en otras pantallas “añadir al carrito” te manda acá
    fun currencyCLP(valor: Int): String =
        NumberFormat.getCurrencyInstance(Locale("es", "CL")).format(valor)
}
