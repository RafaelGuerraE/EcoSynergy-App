package br.ecosynergy_app.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.webkit.WebView.FindListener
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import br.ecosynergy_app.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterActivity : AppCompatActivity() {

    private var hasErrorShown = false
    private var hasErrorShownC = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnBack : ImageButton = findViewById(R.id.btnBack)

        val btnRegister: Button = findViewById(R.id.btnregister)

        val txtEmail: TextInputEditText = findViewById(R.id.txtEmail)

        val passwordLayout: TextInputLayout = findViewById(R.id.passwordLayout)
        val txtPassword: TextInputEditText = findViewById(R.id.txtPassword)

        val confirmPasswordLayout: TextInputLayout = findViewById(R.id.confirmPasswordLayout)
        val txtConfirmPassword: TextInputEditText = findViewById(R.id.txtConfirmPassword)

        btnBack.setOnClickListener{ finish() }

        btnRegister.setOnClickListener {

            val email = txtEmail.text.toString()
            val password = txtPassword.text.toString()
            val confirmPassword = txtConfirmPassword.text.toString()

            if (password.isEmpty() || confirmPassword.isEmpty()) {
                if (password.isEmpty()) {
                    passwordLayout.endIconMode = TextInputLayout.END_ICON_NONE
                    txtPassword.error = "Insira sua senha"
                    txtPassword.requestFocus()
                    hasErrorShown = true
                }
                if (confirmPassword.isEmpty()) {
                    confirmPasswordLayout.endIconMode = TextInputLayout.END_ICON_NONE
                    hasErrorShownC = true
                    txtConfirmPassword.error = "Confirme sua senha"
                    txtConfirmPassword.requestFocus()
                }
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                confirmPasswordLayout.endIconMode = TextInputLayout.END_ICON_NONE
                hasErrorShownC = true
                txtConfirmPassword.error = "As senhas não se correspondem"
                txtConfirmPassword.requestFocus()
                return@setOnClickListener
            }

            val i = Intent(this, RegisterActivity2::class.java).apply {
                putExtra("EMAIL", email)
                putExtra("PASSWORD", password)
            }
            startActivity(i)
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
    }
}