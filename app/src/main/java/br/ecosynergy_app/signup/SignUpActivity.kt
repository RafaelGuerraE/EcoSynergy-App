package br.ecosynergy_app.signup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class SignUpActivity : AppCompatActivity() {

    private var hasErrorShown = false
    private var hasErrorShownC = false

    private var fullname: String = ""
    private var username: String = ""
    private var email: String = ""

    private var gender: String = ""

    private var password: String = ""

    private var nationality: String = ""

    private var steps: Int = 0

    private var nationalityMap: Map<String?, String> = mapOf()

    private lateinit var signUpViewModel: SignUpViewModel


    private lateinit var btnAction: Button
    private lateinit var progressBarAction: ProgressBar

    private lateinit var btnSignUp: Button
    private lateinit var progressBarSignUp: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        signUpViewModel = ViewModelProvider(
            this,
            SignUpViewModelFactory(RetrofitClient.signUpService)
        )[SignUpViewModel::class.java]

        val btnBack: ImageButton = findViewById(R.id.btnBack)

        val txtEmail: EditText = findViewById(R.id.txtEmail)

        val passwordLayout: TextInputLayout = findViewById(R.id.passwordLayout)
        val txtPassword: EditText = findViewById(R.id.txtPassword)

        val confirmPasswordLayout: TextInputLayout = findViewById(R.id.confirmPasswordLayout)
        val txtConfirmPassword: EditText = findViewById(R.id.txtConfirmPassword)

        val txtUsername: EditText = findViewById(R.id.txtUsername)
        val txtFullname: EditText = findViewById(R.id.txtFullname)


        val txtErrorUsername: TextView = findViewById(R.id.txtErrorUsername)


        val txtNationality: MaterialAutoCompleteTextView = findViewById(R.id.txtNationality)
        val autoError: TextView = findViewById(R.id.autoError)

        val spinnerGender: Spinner = findViewById(R.id.spinnerGender)
        val spinnerError: TextView = findViewById(R.id.spinnerError)

        val linearInformation: LinearLayout = findViewById(R.id.linearInformation)
        val linearNationalityGender: LinearLayout = findViewById(R.id.linearNationalityGender)
        val linearPasswords: LinearLayout = findViewById(R.id.linearPasswords)

        btnAction = findViewById(R.id.btnAction)
        progressBarAction = findViewById(R.id.progressBarAction)

        btnSignUp = findViewById(R.id.btnSignUp)
        progressBarSignUp = findViewById(R.id.progressBarSignUp)

        val nationalities = loadNationalities()
        nationalityMap = nationalities.associate { it.nationality_br to it.nationality }

        val nationalityBr = nationalities.map { it.nationality_br }

        val nationalityAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nationalityBr)
        txtNationality.setAdapter(nationalityAdapter)

        txtNationality.setOnItemClickListener { parent, _, position, _ ->
            val selectedNationalityBr = parent.getItemAtPosition(position) as String
            nationality = nationalityMap[selectedNationalityBr] ?: "Unknown"
        }

        btnBack.setOnClickListener { finish() }

        btnAction.setOnClickListener {

            when (steps) {
                0 -> {
                    showButtonLoading(true, btnAction, progressBarAction)
                    signUpViewModel.usernameExists.removeObservers(this)
                    signUpViewModel.checkUsernameExists(txtUsername.text.toString())
                    signUpViewModel.usernameExists.observe(this) { result ->
                        if (result == true) {
                            txtErrorUsername.apply {
                                alpha = 0f
                                visibility = View.VISIBLE
                                animate()
                                    .alpha(1f)
                                    .setDuration(300)
                                    .setListener(null)
                            }
                        } else {
                            txtErrorUsername.visibility = View.GONE

                            fullname = txtFullname.text.toString()
                            username = txtUsername.text.toString()
                            email = txtEmail.text.toString()

                            linearInformation.animate().alpha(0f).setDuration(300).withEndAction {
                                linearInformation.visibility = View.GONE
                                linearNationalityGender.visibility = View.VISIBLE
                            }
                            steps++
                        }
                        showButtonLoading(false, btnAction, progressBarAction)
                    }
                }

                1 -> {
                    showButtonLoading(true, btnAction, progressBarSignUp)
                    val genderSelected = spinnerGender.selectedItem.toString()
                    val nationalitySelected: String = txtNationality.text.toString()

                    if (nationalitySelected.isEmpty() && genderSelected == "Selecione uma opção") {
                        autoError.visibility = TextView.VISIBLE
                        autoError.text = "Selecione uma nacionalidade"
                        txtNationality.requestFocus()
                        spinnerError.visibility = TextView.VISIBLE
                        spinnerError.text = "Selecione uma opção de gênero"
                        return@setOnClickListener
                    } else if (nationalitySelected.isEmpty()) {
                        autoError.visibility = TextView.VISIBLE
                        autoError.text = "Selecione uma nacionalidade"
                        txtNationality.requestFocus()
                        spinnerError.visibility = TextView.INVISIBLE
                        spinnerError.text = null
                        return@setOnClickListener
                    } else if (genderSelected == "Selecione uma opção") {
                        spinnerError.visibility = TextView.VISIBLE
                        spinnerError.text = "Selecione uma opção de gênero"
                        autoError.visibility = TextView.INVISIBLE
                        autoError.text = null
                        return@setOnClickListener
                    }

                    gender = when (genderSelected) {
                        "Masculino" -> "Male"
                        "Feminino" -> "Female"
                        "Outro" -> "Other"
                        else -> "PNS"
                    }

                    linearNationalityGender.animate().alpha(0f).setDuration(300).withEndAction {
                        linearNationalityGender.visibility = View.GONE
                        linearPasswords.visibility = View.VISIBLE
                    }

                    btnAction.animate().alpha(0f).setDuration(300).withEndAction {
                        btnAction.visibility = View.GONE
                        btnSignUp.visibility = View.VISIBLE
                        showButtonLoading(false, btnAction, progressBarAction)
                    }
                }
            }


        }

        txtPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    passwordLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                    passwordLayout.error = null
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (hasErrorShown && s.isNullOrEmpty()) {
                    passwordLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                    hasErrorShown = false
                }
            }
        })

        txtConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    confirmPasswordLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                    confirmPasswordLayout.error = null
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (hasErrorShownC && s.isNullOrEmpty()) {
                    confirmPasswordLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                    hasErrorShownC = false
                }
            }
        })

        btnSignUp.setOnClickListener {
            showButtonLoading(true, btnSignUp, progressBarSignUp)
            val passwordText = txtPassword.text.toString()
            val confirmPasswordText = txtConfirmPassword.text.toString()

            if (passwordText.isEmpty() || confirmPasswordText.isEmpty()) {
                if (passwordText.isEmpty()) {
                    passwordLayout.endIconMode = TextInputLayout.END_ICON_NONE
                    txtPassword.error = "Insira sua senha"
                    txtPassword.requestFocus()
                    hasErrorShown = true
                }
                if (confirmPasswordText.isEmpty()) {
                    confirmPasswordLayout.endIconMode = TextInputLayout.END_ICON_NONE
                    hasErrorShownC = true
                    txtConfirmPassword.error = "Confirme sua senha"
                    txtConfirmPassword.requestFocus()
                }
                return@setOnClickListener
            }

            if (passwordText != confirmPasswordText) {
                confirmPasswordLayout.endIconMode = TextInputLayout.END_ICON_NONE
                hasErrorShownC = true
                txtConfirmPassword.error = "As senhas não se correspondem"
                txtConfirmPassword.requestFocus()
                return@setOnClickListener
            }

            password = passwordText

            val i = Intent(this, EmailConfirmationActivity::class.java).apply {
                putExtra("EMAIL", email)
                putExtra("PASSWORD", password)
                putExtra("FULLNAME", fullname)
                putExtra("USERNAME", username)
                putExtra("NATIONALITY", nationality)
                putExtra("GENDER", gender)
            }
            showButtonLoading(false, btnSignUp, progressBarSignUp)
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

    private fun showButtonLoading(isLoading: Boolean, button: Button, progressBar: ProgressBar) {
        if (isLoading) {
            button.text = ""
            progressBar.visibility = View.VISIBLE
            button.isClickable = false
        } else {
            button.text = when(button.id) {
                R.id.btnAction -> "CONTINUAR"
                R.id.btnSignUp -> "CADASTRAR"
                else -> button.text.toString()
            }
            progressBar.visibility = View.GONE
            button.isClickable = true
        }
    }
}