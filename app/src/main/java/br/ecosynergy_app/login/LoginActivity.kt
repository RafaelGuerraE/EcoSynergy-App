package br.ecosynergy_app.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.appcompat.app.AppCompatActivity
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.home.HomeActivity
import br.ecosynergy_app.register.RegisterActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var txtEntry: EditText
    private lateinit var txtPassword: EditText
    private lateinit var authViewModel: AuthViewModel
    private lateinit var lblReset: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (isLoggedIn()) {
            startHomeActivity()
            return
        }

        authViewModel = ViewModelProvider(this, AuthViewModelFactory(RetrofitClient.authService)).get(AuthViewModel::class.java)

        txtEntry = findViewById(R.id.txtEntry)
        txtPassword = findViewById(R.id.txtPassword)
        val btnLogin: Button = findViewById(R.id.btnLogin)
        val btnRegister: Button = findViewById(R.id.btnRegister)

        btnLogin.setOnClickListener {
            val username = txtEntry.text.toString()
            val password = txtPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                if (username.isEmpty()) {
                    txtEntry.error = "Insira seu Nome de Usuário"
                }
                if (password.isEmpty()) {
                    txtPassword.error = "Insira sua senha"
                }
                return@setOnClickListener
            }

            val loginRequest = LoginRequest(username, password)
            authViewModel.loginUser(loginRequest)
        }

        btnRegister.setOnClickListener {
            val i = Intent(this, RegisterActivity::class.java)
            startActivity(i)
        }

        authViewModel.loginResult.observe(this) { result ->
            result.onSuccess { loginResponse ->
                Log.d("LoginActivity", "Login success")
                setLoggedIn(true, loginResponse.username, loginResponse.accessToken)
                startHomeActivity()
            }.onFailure { error ->
                error.printStackTrace()
                Log.d("LoginActivity", "Login failed: ${error.message}")
                showToast("Dados Incorretos")
            }
        }

        lblReset = findViewById(R.id.lblReset)

        lblReset.setOnClickListener(){
            val i = Intent(this, ResetActivity::class.java)
            startActivity(i)
        }
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

    private fun isLoggedIn(): Boolean {
        val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }
}
