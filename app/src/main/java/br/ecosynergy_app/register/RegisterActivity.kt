package br.ecosynergy_app.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView.FindListener
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import br.ecosynergy_app.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterActivity : AppCompatActivity() {

    private var hasErrorShown = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnRegister: Button = findViewById(R.id.btnregister)
        val btnBack : ImageButton = findViewById(R.id.btnBack)
        val txtPassword: TextInputEditText = findViewById(R.id.txtPassword)
        val txtConfirmPassword: TextInputEditText = findViewById(R.id.txtConfirmPassword)
        val passwordLayout: TextInputLayout = findViewById(R.id.passwordLayout)

        btnBack.setOnClickListener(){
            finish()
        }

        btnRegister.setOnClickListener(){

            val password = txtPassword.text.toString()
            val confirmPassword = txtConfirmPassword.text.toString()

            if (password.isEmpty() || confirmPassword.isEmpty()) {
                if (password.isEmpty()) {
                    passwordLayout.endIconMode = TextInputLayout.END_ICON_NONE
                    txtPassword.error = "Insira sua senha"
                    hasErrorShown = true
                }
                if (confirmPassword.isEmpty()) {
                    passwordLayout.endIconMode = TextInputLayout.END_ICON_NONE
                    txtPassword.error = "Insira sua senha"
                    hasErrorShown = true
                }
                return@setOnClickListener
            }
            if (txtPassword.text.toString() != txtConfirmPassword.text.toString()){
                txtConfirmPassword.error = "As senhas não se correspondem"
            }
            else
            {
                val i = Intent(this, RegisterActivity2::class.java)
                startActivity(i)
            }

        }
    }
}