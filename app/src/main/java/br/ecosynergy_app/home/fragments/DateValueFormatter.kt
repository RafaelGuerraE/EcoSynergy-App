package br.ecosynergy_app.home.fragments

import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateValueFormatter : ValueFormatter() {
    private val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

    override fun getFormattedValue(value: Float): String {
        return dateFormat.format(Date(value.toLong()))
    }
}
