package com.example.currencytrack

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
//import android.text.Html
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import java.text.SimpleDateFormat
import java.util.*

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var menuItem: MenuItem? = null
    private lateinit var tvDollarBuy: TextView
    private lateinit var tvDollarSale: TextView
    private lateinit var tvEuroBuy: TextView
    private lateinit var tvEuroSale: TextView
    private lateinit var tvLastUpdate: TextView
    private lateinit var dateTextView: TextView

    private val privatBankService by lazy {
        RetrofitClient.getRetrofit("https://api.privatbank.ua/").create(PrivatBankApiService::class.java)
    }

    private val nbuService by lazy {
        RetrofitClient.getRetrofit("https://bank.gov.ua/").create(NbuApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Налаштування Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Ініціалізація TextView для курсу долара та євро
        tvDollarBuy = findViewById(R.id.tvDollarBuy)
        tvDollarSale = findViewById(R.id.tvDollarSale)
        tvEuroBuy = findViewById(R.id.tvEuroBuy)
        tvEuroSale = findViewById(R.id.tvEuroSale)
        tvLastUpdate = findViewById(R.id.tvLastUpdate)
        dateTextView= findViewById(R.id.dateTextView)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.bank_menu, menu)

        // Знаходимо елемент меню
        menuItem = menu?.findItem(R.id.action_bank)

        // Створюємо SpannableString з поточного тексту
        val spannableTitle = SpannableString(menuItem?.title)

        // Задаємо білий колір для тексту
        spannableTitle.setSpan(
            ForegroundColorSpan(Color.WHITE),
            0,
            spannableTitle.length,
            0
        )

        // Оновлюємо заголовок елемента меню
        menuItem?.title = spannableTitle

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.bank_nbu -> {
                // Обрано НБУ
                Toast.makeText(this, "Обрано НБУ", Toast.LENGTH_SHORT).show()
                updateMenuTitle("НБУ")
                getNbuExchangeRates()
                updateLastUpdateTime()
                true
            }
            R.id.bank_privat -> {
                // Обрано ПриватБанк
                Toast.makeText(this, "Обрано ПриватБанк", Toast.LENGTH_SHORT).show()
                updateMenuTitle("ПриватБанк")
                getPrivatBankExchangeRates()
                updateLastUpdateTime()
                true
            }
            R.id.bank_oschad -> {
                // Обрано ОщадБанк
                Toast.makeText(this, "Обрано ОщадБанк", Toast.LENGTH_SHORT).show()
                updateMenuTitle("ОщадБанк")
                updateCurrencyRates("-", "-", "-", "-")  // Задаємо курс для ОщадБанку
                updateLastUpdateTime()
                dateTextView.text = ""
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Оновлюємо текст елемента меню
    private fun updateMenuTitle(bankName: String) {

        val spannableTitle = SpannableString(bankName)

        // Задаємо білий колір тексту
        spannableTitle.setSpan(
            ForegroundColorSpan(Color.WHITE),
            0,
            spannableTitle.length,
            0
        )

        menuItem?.title = spannableTitle
    }

    // Оновлюємо курси валют
    @SuppressLint("SetTextI18n")
    private fun updateCurrencyRates(dollarBuy: String, dollarSale: String, euroBuy: String, euroSale: String) {
        tvDollarBuy.text = dollarBuy
        tvDollarSale.text = dollarSale
        tvEuroBuy.text = euroBuy
        tvEuroSale.text = euroSale
    }

    // Оновлюємо час останнього оновлення
    @SuppressLint("SetTextI18n")
    private fun updateLastUpdateTime() {
        val currentTime = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
        tvLastUpdate.text = "Останнє оновлення: $currentTime"
    }

    // Отримуємо курс валют через API ПриватБанку
    private fun getPrivatBankExchangeRates() {
        lifecycleScope.launch {
            try {
                // Отримуємо курс через API
                val rates: List<PrivatBankRatesResponse> = privatBankService.getCashExchangeRates()

                // Знаходимо потрібні курси (USD, EUR)
                val usdRate = rates.find { it.ccy == "USD" }
                val eurRate = rates.find { it.ccy == "EUR" }

                // Якщо курси знайдені, оновлюємо UI
                if (usdRate != null && eurRate != null) {
                    updateCurrencyRates(usdRate.buy, usdRate.sale, eurRate.buy, eurRate.sale)
                    updateLastUpdateTime()
                    dateTextView.text = ""
                } else {
                    Toast.makeText(this@MainActivity, "Не вдалося знайти курс валют", Toast.LENGTH_SHORT).show()
                }

            } catch (e: IOException) {
                // Обробка помилки Інтернет-з'єднання
                Toast.makeText(this@MainActivity, "Перевірте Інтернет-з’єднання", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } catch (e: HttpException) {
                // Обробка HTTP помилок
                Toast.makeText(this@MainActivity, "Сервер повернув помилку: ${e.code()}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Невідома помилка", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    // Отримуємо курс валют через API НБУ
    @SuppressLint("SetTextI18n")
    private fun getNbuExchangeRates() {
        lifecycleScope.launch {
            try {
                val rates = nbuService.getExchangeRates()
                val exchangeDate = rates.firstOrNull()?.exchangedate

                // Знаходимо курси USD і EUR
                val usdRate = rates.find { it.cc == "USD" }
                val eurRate = rates.find { it.cc == "EUR" }

                if (usdRate != null && eurRate != null) {
                    updateCurrencyRates(
                        dollarBuy = usdRate.rate.toString(),
                        dollarSale = usdRate.rate.toString(),
                        euroBuy = eurRate.rate.toString(),
                        euroSale = eurRate.rate.toString()
                    )
                    updateLastUpdateTime()

                    dateTextView.text = "Даний курс передбачений НБУ на $exchangeDate"
                } else {
                    Toast.makeText(this@MainActivity, "Курси валют НБУ не знайдено", Toast.LENGTH_SHORT).show()
                }

            } catch (e: IOException) {
                Toast.makeText(this@MainActivity, "Помилка з Інтернетом", Toast.LENGTH_SHORT).show()
            } catch (e: HttpException) {
                Toast.makeText(this@MainActivity, "Помилка сервера НБУ", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Невідома помилка", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}
