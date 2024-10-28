package br.ecosynergy_app.signup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.signup.viewmodel.SignUpViewModel
import br.ecosynergy_app.signup.viewmodel.SignUpViewModelFactory
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

    private var step: Int = 0

    private var nationalityMap: Map<String?, String> = mapOf()

    private lateinit var signUpViewModel: SignUpViewModel

    private lateinit var btnActionContainer: FrameLayout
    private lateinit var btnAction: Button
    private lateinit var progressBarAction: ProgressBar

    private lateinit var btnSignUpContainer: FrameLayout
    private lateinit var btnSignUp: Button
    private lateinit var progressBarSignUp: ProgressBar

    private lateinit var btnStepBack: TextView

    private lateinit var txtNationality: AutoCompleteTextView

    private lateinit var step2: TextView
    private lateinit var step3: TextView
    private lateinit var midStep1: View
    private lateinit var midStep2: View


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

        txtNationality = findViewById(R.id.txtNationality)
        val autoError: TextView = findViewById(R.id.autoError)

        val spinnerGender: Spinner = findViewById(R.id.spinnerGender)
        val spinnerError: TextView = findViewById(R.id.spinnerError)

        val linearInformation: LinearLayout = findViewById(R.id.linearInformation)
        val linearNationalityGender: LinearLayout = findViewById(R.id.linearNationalityGender)
        val linearPasswords: LinearLayout = findViewById(R.id.linearPasswords)

        btnActionContainer = findViewById(R.id.btnActionContainer)
        btnAction = findViewById(R.id.btnAction)
        progressBarAction = findViewById(R.id.progressBarAction)

        btnSignUpContainer = findViewById(R.id.btnSignUpContainer)
        btnSignUp = findViewById(R.id.btnSignUp)
        progressBarSignUp = findViewById(R.id.progressBarSignUp)

        btnStepBack = findViewById(R.id.btnStepBack)

        step2 = findViewById(R.id.step2)
        step3 = findViewById(R.id.step3)
        midStep1 = findViewById(R.id.midStep1)
        midStep2 = findViewById(R.id.midStep2)

        val nationalities = loadNationalities()
        nationalityMap = nationalities.associate { it.nationality_br to it.nationality }
        val nationalityBr = nationalities.mapNotNull { it.nationality_br }

        val nationalityAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nationalityBr)
        txtNationality.setAdapter(nationalityAdapter)

        txtNationality.setOnItemClickListener { parent, _, position, _ ->
            val selectedNationalityBr = parent.getItemAtPosition(position) as String
            nationality = nationalityMap[selectedNationalityBr] ?: "Unknown"
            Log.d("SignUpActivity", nationality)
        }

        btnBack.setOnClickListener { finish() }

        btnAction.setOnClickListener {

            when (step) {
                0 -> {
                    showButtonLoading(true, btnAction, progressBarAction)
                    signUpViewModel.usernameExists.removeObservers(this)
                    signUpViewModel.checkUsernameExists(txtUsername.text.toString()) {
                        val result = signUpViewModel.usernameExists.value
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
                                linearNationalityGender.alpha = 1f
                                linearNationalityGender.visibility = View.VISIBLE
                            }
                            step++
                            step2.setBackgroundResource(R.drawable.step_active)
                            step2.setTextColor((ContextCompat.getColor(this, R.color.white)))
                            midStep1.setBackgroundColor(ContextCompat.getColor(this, R.color.green))


                            btnStepBack.visibility = View.VISIBLE
                        }

                        showButtonLoading(false, btnAction, progressBarAction)
                    }
                }


                1 -> {
                    showButtonLoading(true, btnAction, progressBarAction)
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
                    } else {
                        spinnerError.visibility = View.INVISIBLE
                        autoError.visibility = View.INVISIBLE
                    }

                    gender = when (genderSelected) {
                        "Masculino" -> "Male"
                        "Feminino" -> "Female"
                        "Outro" -> "Other"
                        else -> "PNS"
                    }


                    step3.setBackgroundResource(R.drawable.step_active)
                    step3.setTextColor((ContextCompat.getColor(this, R.color.white)))
                    midStep2.setBackgroundColor(ContextCompat.getColor(this, R.color.green))

                    linearNationalityGender.animate().alpha(0f).setDuration(300).withEndAction {
                        linearNationalityGender.visibility = View.GONE
                        linearPasswords.alpha = 1f
                        linearPasswords.visibility = View.VISIBLE
                    }

                    btnActionContainer.animate().alpha(0f).setDuration(300).withEndAction {
                        btnActionContainer.visibility = View.GONE
                        btnSignUpContainer.alpha = 1f
                        btnSignUpContainer.visibility = View.VISIBLE
                    }

                    step++

                    showButtonLoading(false, btnAction, progressBarAction)
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
                    showButtonLoading(false, btnSignUp, progressBarSignUp)
                }
                if (confirmPasswordText.isEmpty()) {
                    confirmPasswordLayout.endIconMode = TextInputLayout.END_ICON_NONE
                    hasErrorShownC = true
                    txtConfirmPassword.error = "Confirme sua senha"
                    txtConfirmPassword.requestFocus()

                    showButtonLoading(false, btnSignUp, progressBarSignUp)
                }
                return@setOnClickListener
            }

            if (passwordText != confirmPasswordText) {
                confirmPasswordLayout.endIconMode = TextInputLayout.END_ICON_NONE
                hasErrorShownC = true
                txtConfirmPassword.error = "As senhas não se correspondem"
                showButtonLoading(false, btnSignUp, progressBarSignUp)
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
            Log.d("SignUpActivity", "$email $password $fullname $username $nationality $gender")
            showButtonLoading(false, btnSignUp, progressBarSignUp)
            startActivity(i)
        }

        btnStepBack.setOnClickListener {
            when (step) {
                1 -> {
                    btnStepBack.visibility = View.GONE
                    linearNationalityGender.animate().alpha(0f).setDuration(300).withEndAction {
                        linearInformation.visibility = View.VISIBLE
                        linearInformation.alpha = 1f
                        linearNationalityGender.visibility = View.GONE
                    }
                    step2.setBackgroundResource(R.drawable.step_inactive)
                    step2.setTextColor((ContextCompat.getColor(this, R.color.black)))
                    midStep1.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))


                    step--
                }

                2 -> {
                    step3.setBackgroundResource(R.drawable.step_inactive)
                    step3.setTextColor((ContextCompat.getColor(this, R.color.black)))
                    midStep2.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))

                    linearNationalityGender.animate().alpha(0f).setDuration(300).withEndAction {
                        linearNationalityGender.visibility = View.VISIBLE
                        linearNationalityGender.alpha = 1f
                        linearPasswords.visibility = View.GONE
                    }

                    btnSignUpContainer.animate().alpha(0f).setDuration(300).withEndAction {
                        btnSignUpContainer.visibility = View.GONE
                        btnActionContainer.alpha = 1f
                        btnActionContainer.visibility = View.VISIBLE
                    }

                    step--
                }
            }
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
            button.text = when (button.id) {
                R.id.btnAction -> "Próximo Passo"
                R.id.btnSignUp -> "Realizar Cadastro"
                else -> button.text.toString()
            }
            progressBar.visibility = View.GONE
            button.isClickable = true
        }
    }
}