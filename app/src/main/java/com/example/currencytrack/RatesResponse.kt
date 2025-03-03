package com.example.currencytrack

data class PrivatBankRatesResponse(
    val ccy: String,      // Валюта, для якої ми отримуємо курс (USD, EUR)
    val base_ccy: String, // Валюта, з якої проводиться обмін (UAH)
    val buy: String,      // Курс покупки
    val sale: String      // Курс продажу
)

data class NbuRateResponse(
    val r030: Int,
    val txt: String,
    val rate: Double,
    val cc: String,
    val exchangedate: String?
)
