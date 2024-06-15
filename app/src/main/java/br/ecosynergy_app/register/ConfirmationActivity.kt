package br.ecosynergy_app.register

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.home.HomeActivity
import br.ecosynergy_app.login.AuthViewModel
import br.ecosynergy_app.login.AuthViewModelFactory
import br.ecosynergy_app.login.LoginRequest

class ConfirmationActivity : AppCompatActivity() {

    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)

        val txtResend = findViewById<TextView>(R.id.txtResend)
        val digit1 = findViewById<EditText>(R.id.digit1)
        val digit2 = findViewById<EditText>(R.id.digit2)
        val digit3 = findViewById<EditText>(R.id.digit3)
        val digit4 = findViewById<EditText>(R.id.digit4)

        val btnCheck: Button = findViewById(R.id.btncheck)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val email: String = intent.getStringExtra("EMAIL").toString()
        val password: String = intent.getStringExtra("PASSWORD").toString()
        val fullName: String = intent.getStringExtra("FULLNAME").toString()
        val userName: String = intent.getStringExtra("USERNAME").toString()
        val nationality: String = intent.getStringExtra("NATIONALITY").toString()
        val gender: String = intent.getStringExtra("GENDER").toString()

        registerViewModel = ViewModelProvider(this, RegisterViewModelFactory(RetrofitClient.registerService))[RegisterViewModel::class.java]
        authViewModel = ViewModelProvider(this, AuthViewModelFactory(RetrofitClient.authService))[AuthViewModel::class.java]

        btnBack.setOnClickListener { finish() }

        registerViewModel.registerResult.observe(this) { result ->
            result.onSuccess { createUserResponse ->
                Log.d("ConfirmationActivity", "Register success")
                // Only attempt login after successful registration
                val loginRequest = LoginRequest(userName, password)
                authViewModel.loginUser(loginRequest)
            }.onFailure { error ->
                error.printStackTrace()
                Log.d("ConfirmationActivity", "Register failed: ${error.message}")
                showToast("Erro RegisterResult: ${error.message}")
            }
        }

        authViewModel.loginResult.observe(this) { result ->
            result.onSuccess { loginResponse ->
                Log.d("LoginActivity", "Login success")
                setLoggedIn(true, loginResponse.username, loginResponse.accessToken)
                startHomeActivity()
            }.onFailure { error ->
                error.printStackTrace()
                Log.d("LoginActivity", "Login failed: ${error.message}")
                showToast("Erro LoginResponse: ${error.message}")
            }
        }

        btnCheck.setOnClickListener {
            val createUserRequest = CreateUserRequest(userName, fullName, email, password, gender, nationality)
            registerViewModel.registerUser(createUserRequest)
        }

        txtResend.setOnClickListener{
            txtResend.setTextColor(Color.GRAY)
            startCountDown(txtResend)
        }

        setEditTextFocusChange(digit1, digit2)
        setEditTextFocusChange(digit2, digit3)
        setEditTextFocusChange(digit3, digit4)
    }

    private fun startCountDown(textView: TextView) {
        // Create a CountDownTimer for 60 seconds, updating every second
        object : CountDownTimer(60000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                textView.text = "Reenvie em ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                textView.text = "Reenviar código"
                textView.setTextColor(resources.getColor(R.color.greenDark, null))
            }
        }.start()
    }

    private fun setEditTextFocusChange(currentEditText: EditText, nextEditText: EditText) {
        currentEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    nextEditText.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun startHomeActivity() {
        val i = Intent(this, HomeActivity::class.java)
        startActivity(i)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setLoggedIn(isLoggedIn: Boolean, username: String? = null, accessToken: String? = null) {
        val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", isLoggedIn)
        if (username != null) {
            editor.putString("username", username)
        }
        if (accessToken != null) {
            editor.putString("accessToken", accessToken)
        }
        editor.apply()
    }
}