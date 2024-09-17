package br.ecosynergy_app.register

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.transition.Visibility
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.home.HomeActivity
import br.ecosynergy_app.login.LoginRequest
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.TeamsRepository
import br.ecosynergy_app.room.UserRepository
import br.ecosynergy_app.teams.RoleRequest
import br.ecosynergy_app.teams.TeamsViewModel
import br.ecosynergy_app.teams.TeamsViewModelFactory
import br.ecosynergy_app.user.UserViewModel
import br.ecosynergy_app.user.UserViewModelFactory

class ConfirmationActivity : AppCompatActivity() {

    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var teamsViewModel: TeamsViewModel
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var overlayView: View

    var digit1Text : String = ""
    var digit2Text : String = ""
    var digit3Text : String = ""
    var digit4Text : String = ""

    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)

        val txtResend = findViewById<TextView>(R.id.txtResend)
        val digit1 = findViewById<EditText>(R.id.digit1)
        val digit2 = findViewById<EditText>(R.id.digit2)
        val digit3 = findViewById<EditText>(R.id.digit3)
        val digit4 = findViewById<EditText>(R.id.digit4)

        digit1.setText("1")
        digit2.setText("2")
        digit3.setText("3")
        digit4.setText("4")

        val txtError = findViewById<LinearLayout>(R.id.txtError)

        val btnCheck: Button = findViewById(R.id.btnCheck)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val email: String = intent.getStringExtra("EMAIL").toString()
        val password: String = intent.getStringExtra("PASSWORD").toString()
        val fullName: String = intent.getStringExtra("FULLNAME").toString()
        val userName: String = intent.getStringExtra("USERNAME").toString()
        val nationality: String = intent.getStringExtra("NATIONALITY").toString()
        val gender: String = intent.getStringExtra("GENDER").toString()

        registerViewModel = ViewModelProvider(this, RegisterViewModelFactory(RetrofitClient.registerService))[RegisterViewModel::class.java]

        val userDao = AppDatabase.getDatabase(applicationContext).userDao()
        val userRepository = UserRepository(userDao)

        val teamsDao = AppDatabase.getDatabase(applicationContext).teamsDao()
        val teamsRepository = TeamsRepository(teamsDao)

        userViewModel = ViewModelProvider(this, UserViewModelFactory(RetrofitClient.userService, userRepository))[UserViewModel::class.java]
        teamsViewModel = ViewModelProvider(this, TeamsViewModelFactory(RetrofitClient.teamsService, teamsRepository))[TeamsViewModel::class.java]

        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        overlayView = findViewById(R.id.overlayView)

        btnBack.setOnClickListener { finish() }

        registerViewModel.registerResult.observe(this) { result ->
            showProgressBar(false)
            result.onSuccess { response ->
                Log.d("ConfirmationActivity", "Register success")
                val loginRequest = LoginRequest(userName, password)
                userViewModel.loginUser(loginRequest)
                userId = response.id
            }.onFailure { error ->
                error.printStackTrace()
                Log.d("ConfirmationActivity", "Register failed: ${error.message}")
                showToast("Erro RegisterResult: ${error.message}")
            }
        }

        userViewModel.loginResult.observe(this) { result ->
            showProgressBar(false)
            result.onSuccess { loginResponse ->
                Log.d("LoginActivity", "UserID: $userId")
                Log.d("LoginActivity", "Login success")
                setLoggedIn(true, loginResponse.username, loginResponse.accessToken)
                teamsViewModel.addMember(loginResponse.accessToken, 89, userId, RoleRequest("COMMON_USER"))
                startHomeActivity()
            }.onFailure { error ->
                error.printStackTrace()
                Log.d("LoginActivity", "Login failed: ${error.message}")
                showToast("Erro LoginResponse: ${error.message}")
            }
        }

        btnCheck.setOnClickListener {
            digit1Text = digit1.text.toString()
            digit2Text = digit2.text.toString()
            digit3Text = digit3.text.toString()
            digit4Text = digit4.text.toString()

            if(digit1Text != "1" || digit2Text != "2" || digit3Text != "3" || digit4Text != "4")
            {
                txtError.visibility = View.VISIBLE
            }
            else{
                txtError.visibility = View.INVISIBLE
                showProgressBar(true)
                val createUserRequest = CreateUserRequest(userName, fullName, email, password, gender, nationality)
                registerViewModel.registerUser(createUserRequest)
            }
        }

        txtResend.setOnClickListener {
            txtResend.setTextColor(Color.GRAY)
            startCountDown(txtResend)
        }

        setEditTextFocusChange(digit1, digit2)
        setEditTextFocusChange(digit2, digit3)
        setEditTextFocusChange(digit3, digit4)
    }

    private fun showProgressBar(show: Boolean) {
        loadingProgressBar.visibility = if (show) View.VISIBLE else View.GONE
        overlayView.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun startCountDown(textView: TextView) {
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
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setLoggedIn(isLoggedIn: Boolean, identifier: String? = null, accessToken: String? = null) {
        val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", isLoggedIn)
        if (identifier != null) {
            editor.putString("identifier", identifier)
        }
        if (accessToken != null) {
            editor.putString("accessToken", accessToken)
        }
        editor.apply()
    }
}
