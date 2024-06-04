package br.ecosynergy_app.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import br.ecosynergy_app.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class RegisterActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register2)

        val txtNationality: AutoCompleteTextView = findViewById(R.id.txtNationality)
        val btnRegister: Button = findViewById(R.id.btnregister)
        val spinnerGender: Spinner = findViewById(R.id.spinnerGender)
        val spinnerError: TextView = findViewById(R.id.spinnerError)

        val nationalities = loadNationalities()
        val nationalityNames = nationalities.map { it.nationality }

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nationalityNames)
        txtNationality.setAdapter(adapter)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        btnBack.setOnClickListener(){
            finish()
        }

        btnRegister.setOnClickListener {

            val nationalitySelected = txtNationality.text.toString()
            val gender = spinnerGender.selectedItem.toString()
            if(nationalitySelected == null || gender == "Selecione uma opção")
            {
                if(nationalitySelected == null){
                    txtNationality.error = "Selecione uma nacionalidade"
                    txtNationality.requestFocus()
                    return@setOnClickListener
                }
                else{
                    spinnerError.visibility = TextView.VISIBLE
                    spinnerError.text = "Selecione uma opção de gênero"
                    return@setOnClickListener
                }
            }
            if(gender != "Selecione uma opção"){
                spinnerError.visibility = TextView.INVISIBLE
            }

            val i = Intent(this, ConfirmationActivity::class.java)
            startActivity(i)

        }
    }
    private fun loadNationalities(): List<Nationality> {
        val jsonFileString = getData("nationalities.json")
        val gson = Gson()
        val listNationalityType = object : TypeToken<List<Nationality>>() {}.type
        return gson.fromJson(jsonFileString, listNationalityType)
    }

    private fun getData(fileName: String): String? {
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

