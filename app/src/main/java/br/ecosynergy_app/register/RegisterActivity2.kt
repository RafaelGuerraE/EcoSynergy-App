package br.ecosynergy_app.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import br.ecosynergy_app.R
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class RegisterActivity2 : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register2)

        val email = intent.getStringExtra("EMAIL")
        val password = intent.getStringExtra("PASSWORD")

        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        val btnRegister: Button = findViewById(R.id.btnRegister)

        val txtUsername: TextInputEditText = findViewById(R.id.txtUsername)
        val txtFullname: TextInputEditText = findViewById(R.id.txtFullname)

        val txtNationality: AutoCompleteTextView = findViewById(R.id.txtNationality)
        val autoError: TextView = findViewById(R.id.autoError)

        val spinnerGender: Spinner = findViewById(R.id.spinnerGender)
        val spinnerError: TextView = findViewById(R.id.spinnerError)

        val nationalities = loadNationalities()
        val nationalityNames = nationalities.map { it.nationality }

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nationalityNames)
        txtNationality.setAdapter(adapter)

        txtUsername.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val lowercaseText = s.toString().lowercase()

                if (s.toString() != lowercaseText) {
                    txtUsername.setText(lowercaseText)
                    txtUsername.setSelection(lowercaseText.length)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnBack.setOnClickListener{ finish() }

        btnRegister.setOnClickListener {

            val gender = spinnerGender.selectedItem.toString()
            var genderEnglish: String = null.toString()
            val nationalitySelected: String = txtNationality.text.toString()
            val fullName = txtFullname.text.toString()
            val username = txtUsername.text.toString()

            if(nationalitySelected.isEmpty() && gender == "Selecione uma opção")
            {
                autoError.visibility = TextView.VISIBLE
                autoError.text = "Selecione uma nacionalidade"
                txtNationality.requestFocus()
                spinnerError.visibility = TextView.VISIBLE
                spinnerError.text = "Selecione uma opção de gênero"
                return@setOnClickListener
            }
            else if(nationalitySelected.isEmpty()){
                autoError.visibility = TextView.VISIBLE
                autoError.text = "Selecione uma nacionalidade"
                txtNationality.requestFocus()
                spinnerError.visibility = TextView.INVISIBLE
                spinnerError.text = null
                return@setOnClickListener
            }
            else if(gender == "Selecione uma opção"){
                spinnerError.visibility = TextView.VISIBLE
                spinnerError.text = "Selecione uma opção de gênero"
                autoError.visibility = TextView.INVISIBLE
                autoError.text = null
                return@setOnClickListener
            }

            if(gender == "Masculino"){
                genderEnglish = "Male"
            }
            else if(gender == "Feminino"){
                genderEnglish = "Female"
            }
            else if(gender == "Outro"){
                genderEnglish = "Other"
            }
            else{
                genderEnglish = "PNS"
            }

            val i = Intent(this, ConfirmationActivity::class.java).apply {
                putExtra("EMAIL", email)
                putExtra("PASSWORD", password)
                putExtra("FULLNAME", fullName)
                putExtra("USERNAME", username)
                putExtra("NATIONALITY", nationalitySelected)
                putExtra("GENDER", genderEnglish)
            }
            startActivity(i)
        }
    }
    private fun loadNationalities(): List<Nationality> {
        val jsonFileString = getNationality("nationalities.json")
        val gson = Gson()
        val listNationalityType = object : TypeToken<List<Nationality>>() {}.type
        return gson.fromJson(jsonFileString, listNationalityType)
    }

    private fun getNationality(fileName: String): String? {
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

