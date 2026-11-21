package com.matjard.core

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/** Format a DZD amount like "199,000 DA" (always latin digits, stable in both locales) */
fun formatPrice(amount: Double): String {
    val symbols = DecimalFormatSymbols(Locale.US).apply { groupingSeparator = ',' }
    return DecimalFormat("#,###", symbols).format(amount) + " DA"
}

/** "MD-0001" style order code */
fun orderCode(orderNumber: Long): String = "MD-" + orderNumber.toString().padStart(4, '0')

/** 58 Algerian wilayas (Arabic names) for the checkout dropdown */
val WILAYAS: List<String> = listOf(
    "أدرار", "الشلف", "الأغواط", "أم البواقي", "باتنة", "بجاية", "بسكرة", "بشار",
    "البليدة", "البويرة", "تمنراست", "تبسة", "تلمسان", "تيارت", "تيزي وزو", "الجزائر العاصمة",
    "الجلفة", "جيجل", "سطيف", "سعيدة", "سكيكدة", "سيدي بلعباس", "عنابة", "قالمة",
    "قسنطينة", "المدية", "مستغانم", "المسيلة", "معسكر", "ورقلة", "وهران", "البيض",
    "برج بوعريريج", "بومرداس", "الطارف", "تندوف", "تيسمسيلت", "إيليزي", "الوادي", "خنشلة",
    "سوق أهراس", "تيبازة", "ميلة", "عين الدفلى", "النعامة", "عين تموشنت", "غرداية", "غليزان",
    "تيميمون", "برج باجي مختار", "أولاد جلال", "بني عباس", "عين صالح", "عين قزام",
    "تقرت", "جانت", "المغير", "سوق الفلة",
)

const val STATUS_NEW = "new"
const val STATUS_PREPARING = "preparing"
const val STATUS_OUT = "out_for_delivery"
const val STATUS_DELIVERED = "delivered"
const val STATUS_CANCELLED = "cancelled"

fun nextStatus(current: String): String? = when (current) {
    STATUS_NEW -> STATUS_PREPARING
    STATUS_PREPARING -> STATUS_OUT
    STATUS_OUT -> STATUS_DELIVERED
    else -> null
}
