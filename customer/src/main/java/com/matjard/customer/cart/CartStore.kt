package com.matjard.customer.cart

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.matjard.core.Product

data class CartItem(
    val productId: String,
    val nameAr: String,
    val nameFr: String,
    val price: Double,
    val imageUrl: String?,
    val stock: Int,
    val quantity: Int,
) {
    fun name(lang: String) = if (lang == "fr") nameFr else nameAr
    fun lineTotal(): Double = price * quantity
}

/** Cart persisted in SharedPreferences as JSON; bumped version notifies the UI. */
class CartStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("matjard_cart", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun items(): List<CartItem> {
        val raw = prefs.getString(KEY_ITEMS, null) ?: return emptyList()
        return try {
            gson.fromJson(raw, object : TypeToken<List<CartItem>>() {}.type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun count(): Int = items().sumOf { it.quantity }

    fun subtotal(): Double = items().sumOf { it.lineTotal() }

    fun add(product: Product, quantity: Int = 1) {
        val current = items().toMutableList()
        val index = current.indexOfFirst { it.productId == product.id }
        if (index >= 0) {
            val existing = current[index]
            val newQty = (existing.quantity + quantity).coerceAtMost(maxOf(product.stock, 1))
            current[index] = existing.copy(quantity = newQty, price = product.price, stock = product.stock)
        } else {
            current += CartItem(
                productId = product.id,
                nameAr = product.nameAr,
                nameFr = product.nameFr,
                price = product.price,
                imageUrl = product.imageUrl,
                stock = product.stock,
                quantity = quantity.coerceAtMost(maxOf(product.stock, 1)),
            )
        }
        save(current)
    }

    fun setQuantity(productId: String, quantity: Int) {
        val current = items().toMutableList()
        val index = current.indexOfFirst { it.productId == productId }
        if (index < 0) return
        if (quantity <= 0) {
            current.removeAt(index)
        } else {
            current[index] = current[index].copy(quantity = quantity.coerceAtMost(maxOf(current[index].stock, 1)))
        }
        save(current)
    }

    fun remove(productId: String) {
        save(items().filterNot { it.productId == productId })
    }

    fun clear() = prefs.edit().remove(KEY_ITEMS).apply()

    private fun save(items: List<CartItem>) {
        prefs.edit().putString(KEY_ITEMS, gson.toJson(items)).apply()
    }

    // Remember checkout info for faster repeat orders
    fun savedCustomer(): Pair<String, String> =
        (prefs.getString(KEY_NAME, "") ?: "") to (prefs.getString(KEY_PHONE, "") ?: "")

    fun saveCustomer(name: String, phone: String) {
        prefs.edit().putString(KEY_NAME, name).putString(KEY_PHONE, phone).apply()
    }

    companion object {
        private const val KEY_ITEMS = "items"
        private const val KEY_NAME = "customer_name"
        private const val KEY_PHONE = "customer_phone"
    }
}
