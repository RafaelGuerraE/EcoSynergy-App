package br.ecosynergy_app.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.ecosynergy_app.R

class HelpActivity : AppCompatActivity() {
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        btnBack = findViewById(R.id.btnBack)

        val txtEmail = findViewById<TextView>(R.id.txtEmail)
        val txtNumber = findViewById<TextView>(R.id.txtNumber)

        txtEmail.apply {
            text = "contato@ecosynergybr.com"
            setTextColor(resources.getColor(R.color.greenDark, null))
            paint.isUnderlineText = true
        }

        txtNumber.apply {
            text = "+55 11 99023-8074"
            setTextColor(resources.getColor(R.color.greenDark, null))
            paint.isUnderlineText = true
        }

        txtEmail.setOnLongClickListener {
            copyToClipboard("Email copiado para a área de transferência", txtEmail.text.toString())
            true
        }

        txtNumber.setOnLongClickListener {
            copyToClipboard("Número copiado para a área de transferência", txtNumber.text.toString())
            true
        }

        txtEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${txtEmail.text}")
            }
            startActivity(intent)
        }

        txtNumber.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${txtNumber.text}")
            }
            startActivity(intent)
        }

        btnBack.setOnClickListener{ finish() }
    }

    private fun copyToClipboard(message: String, text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}