package br.ecosynergy_app.register

import android.content.Intent
import android.graphics.Color
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import br.ecosynergy_app.home.HomeActivity
import br.ecosynergy_app.R
import org.w3c.dom.Text

class ConfirmationActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)

        val btnCheck: Button = findViewById(R.id.btncheck)
        val btnBack: ImageButton = findViewById(R.id.btnBack)
        val txtResend: TextView = findViewById(R.id.txtResend)
        val digit1 = findViewById<EditText>(R.id.digit1)
        val digit2 = findViewById<EditText>(R.id.digit2)
        val digit3 = findViewById<EditText>(R.id.digit3)
        val digit4 = findViewById<EditText>(R.id.digit4)

        btnBack.setOnClickListener{ finish() }

        btnCheck.setOnClickListener{
            val i = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
            finish()
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
}