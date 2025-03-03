package com.example.currencytrack

import retrofit2.http.GET
import retrofit2.http.Query

// ПриватБанк
interface PrivatBankApiService {
    @GET("p24api/pubinfo?json&exchange&coursid=5")
    suspend fun getCashExchangeRates(): List<PrivatBankRatesResponse>
}

// НБУ
interface NbuApiService {
    @GET("NBUStatService/v1/statdirectory/exchange?json")
    suspend fun getExchangeRates(): List<NbuRateResponse>
}