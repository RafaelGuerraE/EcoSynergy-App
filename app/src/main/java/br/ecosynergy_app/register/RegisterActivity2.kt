package br.ecosynergy_app.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import br.ecosynergy_app.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class RegisterActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity2_register)

        val txtNationality: AutoCompleteTextView = findViewById(R.id.txtNationality)
        val btnRegister: Button = findViewById(R.id.btnregister)

        val nationalities = loadNationalitiesFromJson()
        val nationalityNames = nationalities.map { it.nationality }

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nationalityNames)
        txtNationality.setAdapter(adapter)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        btnBack.setOnClickListener(){
            finish()
        }

        btnRegister.setOnClickListener {
            val i = Intent(this, ConfirmationActivity::class.java)
            startActivity(i)
        }
    }
    private fun loadNationalitiesFromJson(): List<Nationality> {
        val jsonFileString = getJsonDataFromAsset("nationalities.json")
        val gson = Gson()
        val listNationalityType = object : TypeToken<List<Nationality>>() {}.type
        return gson.fromJson(jsonFileString, listNationalityType)
    }

    private fun getJsonDataFromAsset(fileName: String): String? {
        return try {
            val inputStream = assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }
}

