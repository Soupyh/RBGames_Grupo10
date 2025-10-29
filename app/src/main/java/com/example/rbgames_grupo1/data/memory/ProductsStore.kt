package com.example.rbgames_grupo1.data.memory

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Modelo UI usado por Admin y compra
data class AdminProductUi(
    val id: Long,
    val nombre: String,
    val categoria: String,
    val precioCLP: Int,
    val stock: Int,
    val activo: Boolean
)


 //Store simple en memoria para compartir el estado de productos
 //entre AdminProductosScreen y el flujo de compra.

object ProductsStore {
    // Datos demo iniciales (puedes precargarlos desde Room luego)
    private val _productos = MutableStateFlow(
        listOf(
            AdminProductUi(1, "Destiny 2", "Acción",        44_990, 12, true),
            AdminProductUi(2, "GTA V",     "Mundo abierto", 62_990,  5, true),
            AdminProductUi(3, "L4D2",      "Shooter",       19_990,  0, false),
            AdminProductUi(4, "Payday 2",  "Cooperativo",   39_990, 21, true)
        )
    )
    val productos: StateFlow<List<AdminProductUi>> = _productos.asStateFlow()

    // Reemplaza un producto (update)
    fun update(p: AdminProductUi) {
        _productos.value = _productos.value.map { if (it.id == p.id) p else it }
    }

    // Agrega producto
    fun add(p: AdminProductUi) {
        _productos.value = _productos.value + p
    }

    // Elimina producto
    fun delete(id: Long) {
        _productos.value = _productos.value.filterNot { it.id == id }
    }

    // Cambia stock por delta (nunca < 0)
    fun changeStock(id: Long, delta: Int) {
        _productos.value = _productos.value.map {
            if (it.id == id) it.copy(stock = (it.stock + delta).coerceAtLeast(0)) else it
        }
    }

    // Descuenta stock por una compra de cantidad dada
    fun discount(id: Long, qty: Int) {
        if (qty <= 0) return
        changeStock(id, -qty)
    }

    // Descuenta stock por todos los ítems del carrito
    data class CartLine(val productId: Long, val quantity: Int)
    fun discountByCart(lines: List<CartLine>) {
        lines.forEach { discount(it.productId, it.quantity) }
    }
}
