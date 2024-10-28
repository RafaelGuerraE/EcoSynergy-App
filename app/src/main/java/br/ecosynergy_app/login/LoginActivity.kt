package br.ecosynergy_app.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.home.HomeActivity
import br.ecosynergy_app.readings.ReadingsViewModel
import br.ecosynergy_app.readings.ReadingsViewModelFactory
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.invites.InvitesRepository
import br.ecosynergy_app.room.readings.ReadingsRepository
import br.ecosynergy_app.room.teams.MembersRepository
import br.ecosynergy_app.room.teams.TeamsRepository
import br.ecosynergy_app.room.user.UserRepository
import br.ecosynergy_app.signup.SignUpActivity
import br.ecosynergy_app.teams.viewmodel.TeamsViewModel
import br.ecosynergy_app.teams.viewmodel.TeamsViewModelFactory
import br.ecosynergy_app.user.viewmodel.UserViewModel
import br.ecosynergy_app.user.viewmodel.UserViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    private lateinit var txtEntry: TextInputEditText
    private lateinit var txtPassword: TextInputEditText

    private lateinit var lblReset: TextView
    private var hasErrorShown = false
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var overlayView: View

    private lateinit var userViewModel: UserViewModel
    private lateinit var teamsViewModel: TeamsViewModel
    private lateinit var readingsViewModel: ReadingsViewModel

    private lateinit var loginSp: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        loginSp = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

        val logoutMessage = intent.getStringExtra("LOGOUT_MESSAGE")
        if (logoutMessage != null) {
            showSnackBar("Você foi desconectado", "FECHAR", R.color.grayDark, this)
        }

        if (isLoggedIn()) {
            startHomeActivity()
            return
        }

        val userRepository = UserRepository(AppDatabase.getDatabase(applicationContext).userDao())
        val teamsRepository =
            TeamsRepository(AppDatabase.getDatabase(applicationContext).teamsDao())
        val readingsRepository = ReadingsRepository(
            AppDatabase.getDatabase(applicationContext).mq7ReadingsDao(),
            AppDatabase.getDatabase(applicationContext).mq135ReadingsDao(),
            AppDatabase.getDatabase(applicationContext).fireReadingsDao()
        )
        val invitesRepository =
            InvitesRepository(AppDatabase.getDatabase(applicationContext).invitesDao())
        val membersRepository =
            MembersRepository(AppDatabase.getDatabase(applicationContext).membersDao())

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(RetrofitClient.userService, userRepository)
        )[UserViewModel::class.java]
        teamsViewModel = ViewModelProvider(
            this,
            TeamsViewModelFactory(
                RetrofitClient.teamsService,
                teamsRepository,
                RetrofitClient.invitesService,
                membersRepository,
                invitesRepository
            )
        )[TeamsViewModel::class.java]
        readingsViewModel = ViewModelProvider(
            this,
            ReadingsViewModelFactory(RetrofitClient.readingsService, readingsRepository)
        )[ReadingsViewModel::class.java]


        txtEntry = findViewById(R.id.txtEntry)
        txtPassword = findViewById(R.id.txtPassword)
        val passwordLayout: TextInputLayout = findViewById(R.id.passwordLayout)
        val btnLogin: MaterialButton = findViewById(R.id.btnLogin)
        val btnRegister: MaterialButton = findViewById(R.id.btnRegister)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        overlayView = findViewById(R.id.overlayView)

        btnLogin.setOnClickListener { view ->
            val username = txtEntry.text.toString()
            val password = txtPassword.text.toString()

            loginUser(view, username, password, passwordLayout)
        }

        txtEntry.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val lowercaseText = s.toString().lowercase()

                if (s.toString() != lowercaseText) {
                    txtEntry.setText(lowercaseText)
                    txtEntry.setSelection(lowercaseText.length)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

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

        btnRegister.setOnClickListener {
            val i = Intent(this, SignUpActivity::class.java)
            startActivity(i)
        }

        lblReset = findViewById(R.id.lblReset)

        lblReset.setOnClickListener {
            val i = Intent(this, RecoverPasswordActivity::class.java)
            startActivity(i)
        }
    }

    private fun startHomeActivity() {
        val i = Intent(this, HomeActivity::class.java)
        startActivity(i)
        finish()
    }

    private fun loginUser(
        view: View,
        username: String,
        password: String,
        passwordLayout: TextInputLayout
    ) {


        if (username.isEmpty() || password.isEmpty()) {
            if (username.isEmpty()) {
                txtEntry.error = "Insira seu Nome de Usuário"
                txtEntry.requestFocus()
            }
            if (password.isEmpty()) {
                passwordLayout.endIconMode = TextInputLayout.END_ICON_NONE
                txtPassword.error = "Insira sua senha"
                txtPassword.requestFocus()
                hasErrorShown = true
            }
            return
        }

        txtEntry.error = null
        txtPassword.error = null

        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)

        // Show loading indicator
        loadingProgressBar.visibility = View.VISIBLE
        overlayView.visibility = View.VISIBLE

        val loginRequest = LoginRequest(username, password)

        // Trigger login attempt
        userViewModel.loginUser(loginRequest) {

            userViewModel.loginResult.observe(this) { result ->
                result.onSuccess { loginResponse ->
                    Log.d("LoginActivity", "Login success")
                    userViewModel.user.observe(this) { result ->
                        result.onSuccess { userData ->
                            userViewModel.insertUserInfoDB(
                                userData,
                                loginResponse.accessToken,
                                loginResponse.refreshToken
                            )
                            teamsViewModel.getTeamsByUserId(
                                userData.id,
                                loginResponse.accessToken
                            ) {
                                setLoggedIn(true)
                                startHomeActivity()
                                loginSp.edit().apply {
                                    putBoolean("just_logged_in", true)
                                    putBoolean("open", true)
                                    apply()
                                }
                            }
                        }
                        // Remove observer after success
                        userViewModel.user.removeObservers(this)
                    }
                }.onFailure { error ->
                    // Hide loading indicator
                    loadingProgressBar.visibility = View.GONE
                    overlayView.visibility = View.GONE

                    Log.d("LoginActivity", "Login failed: ${error.message}")
                    txtEntry.error =
                        "Usuário/Email ou Senha incorreto! Por favor verifique seus dados"
                    txtPassword.text = null
                    txtEntry.requestFocus()
                }
                // Remove observer after receiving a result
                userViewModel.loginResult.removeObservers(this)
            }
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setLoggedIn(isLoggedIn: Boolean) {
        val editor = loginSp.edit()
        editor.putBoolean("isLoggedIn", isLoggedIn)
        editor.apply()
    }

    private fun isLoggedIn(): Boolean {
        return loginSp.getBoolean("isLoggedIn", false)
    }

    private fun showSnackBar(message: String, action: String, bgTint: Int, context: Context) {
        val rootView = findViewById<View>(android.R.id.content)
        val snackBar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT)
            .setAction(action) {}
        snackBar.setBackgroundTint(ContextCompat.getColor(context, bgTint))
        snackBar.setTextColor(ContextCompat.getColor(context, R.color.white))
        snackBar.setActionTextColor(ContextCompat.getColor(context, R.color.white))
        snackBar.show()
    }
}
