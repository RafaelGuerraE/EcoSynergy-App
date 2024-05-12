package br.ecosynergy_app.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import br.ecosynergy_app.HomeActivity
import br.ecosynergy_app.R
import br.ecosynergy_app.register.RegisterActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.login.LoginRequest
import br.ecosynergy_app.login.UserResponse



class LoginActivity : AppCompatActivity() {

    private lateinit var txtEntry: EditText
    private lateinit var txtPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        txtEntry = findViewById(R.id.txtemail)
        txtPassword = findViewById(R.id.txtpassword)
        val btnLogin: Button = findViewById(R.id.btnlogin)
        val btnRegister: Button = findViewById(R.id.btnregister)

        btnLogin.setOnClickListener {
            val username = txtEntry.text.toString()
            val password = txtPassword.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                val loginRequest = LoginRequest(username, password)
                loginUser(loginRequest)
            }
        }

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private suspend fun loginUser(username: String, password: String) {
        val loginUserRequest = LoginRequest(username, password)

        RetrofitClient.authService.loginUser(loginUserRequest).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    // Handle successful login response, e.g., save user details, navigate to home activity
                    userResponse?.let {
                        startHomeActivity()
                    }
                } else {
                    // Handle unsuccessful login response (e.g., invalid credentials)
                    // Show error message to the user
                    // For demo purposes, display a toast
                    showToast("Login failed. Please check your credentials.")
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                // Handle network failures
                // Show error message to the user
                // For demo purposes, display a toast
                showToast("Login failed. Please try again.")
            }
        })
    }


    override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                // Handle network failures
                // Show error message to the user
                // For demo purposes, display a toast
                showToast("Login failed. Please try again.")
            }
        })
    }

    private fun startHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}






