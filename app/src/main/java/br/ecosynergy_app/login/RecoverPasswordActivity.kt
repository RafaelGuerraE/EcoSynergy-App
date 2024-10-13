package br.ecosynergy_app.login

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.signup.EmailConfirmationActivity
import br.ecosynergy_app.signup.SignUpViewModel
import br.ecosynergy_app.signup.SignUpViewModelFactory
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RecoverPasswordActivity : AppCompatActivity() {

    private lateinit var registerViewModel: SignUpViewModel

    private lateinit var btnBack: ImageButton
    private lateinit var btnSend: Button
    private lateinit var btnCheck: Button
    private lateinit var btnChangePassword: Button
    private lateinit var txtResend: TextView
    private lateinit var txtEmailShow: TextView
    private lateinit var txtEmail: TextInputEditText
    private lateinit var txtPassword: TextInputEditText
    private lateinit var txtConfirmPassword: TextInputEditText
    private lateinit var txtError: LinearLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var digit1: EditText
    private lateinit var digit2: EditText
    private lateinit var digit3: EditText
    private lateinit var digit4: EditText
    private lateinit var digit5: EditText
    private lateinit var digit6: EditText
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var overlayView: View
    private lateinit var linearVerification: LinearLayout
    private lateinit var linearEmail: LinearLayout
    private lateinit var linearPasswords: LinearLayout

    private var digit1Text: String = ""
    private var digit2Text: String = ""
    private var digit3Text: String = ""
    private var digit4Text: String = ""
    private var digit5Text: String = ""
    private var digit6Text: String = ""

    private var verificationCode: String = ""
    private var email: String = ""

    private var hasErrorShown = false
    private var hasErrorShownC = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recoverpassword)

        registerViewModel = ViewModelProvider(
            this,
            SignUpViewModelFactory(RetrofitClient.signUpService)
        )[SignUpViewModel::class.java]

        btnBack = findViewById(R.id.btnBack)
        btnSend = findViewById(R.id.btnSend)
        btnCheck = findViewById(R.id.btnCheck)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        txtResend = findViewById(R.id.txtResend)
        txtEmailShow = findViewById(R.id.txtEmailShow)
        txtEmail = findViewById(R.id.txtEmail)
        txtPassword = findViewById(R.id.txtPassword)
        txtConfirmPassword = findViewById(R.id.txtConfirmPassword)
        txtError = findViewById(R.id.txtError)
        passwordLayout = findViewById(R.id.passwordLayout)
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout)
        digit1 = findViewById(R.id.digit1)
        digit2 = findViewById(R.id.digit2)
        digit3 = findViewById(R.id.digit3)
        digit4 = findViewById(R.id.digit4)
        digit5 = findViewById(R.id.digit5)
        digit6 = findViewById(R.id.digit6)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        overlayView = findViewById(R.id.overlayView)
        linearVerification = findViewById(R.id.linearVerification)
        linearEmail = findViewById(R.id.linearEmail)
        linearPasswords = findViewById(R.id.linearPasswords)

        btnBack.setOnClickListener {finish()}

        btnSend.setOnClickListener {
            if(txtEmail.text != null){
                email = txtEmail.text.toString()
                forgotPasswordCode(email)
                linearEmail.animate().alpha(0f).setDuration(300).withEndAction {
                    linearEmail.visibility = View.GONE
                    linearVerification.visibility = View.VISIBLE
                }
            }
            else{
                LoginActivity().showToast("Insira o seu email", this)
            }
        }

        btnCheck.setOnClickListener {
            digit1Text = digit1.text.toString()
            digit2Text = digit2.text.toString()
            digit3Text = digit3.text.toString()
            digit4Text = digit4.text.toString()
            digit5Text = digit5.text.toString()
            digit6Text = digit6.text.toString()

            if (verificationCode.length == 6) {
                val isCodeMatched =
                    digit1Text == verificationCode[0].toString() &&
                            digit2Text == verificationCode[1].toString() &&
                            digit3Text == verificationCode[2].toString() &&
                            digit4Text == verificationCode[3].toString() &&
                            digit5Text == verificationCode[4].toString() &&
                            digit6Text == verificationCode[5].toString()

                if (isCodeMatched) {
                    txtError.visibility = View.INVISIBLE
                    showProgressBar(true)

                    linearVerification.animate().alpha(0f).setDuration(300).withEndAction {
                        linearVerification.visibility = View.GONE
                        linearPasswords.visibility = View.VISIBLE

                        showProgressBar(false)
                    }

                } else {
                    txtError.visibility = View.VISIBLE
                }
            } else {
                LoginActivity().showToast("ERRO NO ENVIO DO CÓDIGO", this)
            }
        }

        btnChangePassword.setOnClickListener {
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

            showProgressBar(true)
            changePassword(txtPassword.text.toString())
        }

        txtResend.setOnClickListener {
            txtResend.isClickable = false
            txtResend.setTextColor(Color.GRAY)
            EmailConfirmationActivity().startCountDown(txtResend)
            forgotPasswordCode(email)
        }

        EmailConfirmationActivity().setEditTextFocusChange(digit1, digit2, null)
        EmailConfirmationActivity().setEditTextFocusChange(digit2, digit3, digit1)
        EmailConfirmationActivity().setEditTextFocusChange(digit3, digit4, digit2)
        EmailConfirmationActivity().setEditTextFocusChange(digit4, digit5, digit3)
        EmailConfirmationActivity().setEditTextFocusChange(digit5, digit6, digit4)
        EmailConfirmationActivity().setEditTextFocusChange(digit6, digit6, digit5)


        EmailConfirmationActivity().digitListener(digit1)
        EmailConfirmationActivity().digitListener(digit2)
        EmailConfirmationActivity().digitListener(digit3)
        EmailConfirmationActivity().digitListener(digit4)
        EmailConfirmationActivity().digitListener(digit5)
        EmailConfirmationActivity().digitListener(digit6)
    }

    private fun showProgressBar(show: Boolean) {
        loadingProgressBar.visibility = if (show) View.VISIBLE else View.GONE
        overlayView.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun changePassword(password: String){
        finish()
    }

    private fun forgotPasswordCode(userEmail: String) {
        showProgressBar(true)
        registerViewModel.verificationCode.removeObservers(this)
        registerViewModel.forgotPasswordCode(userEmail)
        registerViewModel.verificationCode.observe(this) { code ->
            Log.d("ConfirmationActivity", "Confirmation code: $code")
            if (code != null) {
                verificationCode = code
            }
            showProgressBar(false)
        }
    }
}
