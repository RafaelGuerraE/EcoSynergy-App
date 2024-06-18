package br.ecosynergy_app.login

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.ecosynergy_app.R

class ResetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnSend = findViewById<Button>(R.id.btnSend)
        val txtTime = findViewById<TextView>(R.id.txtTime)

        btnBack.setOnClickListener(){
            finish()
        }

        btnSend.setOnClickListener{
            txtTime.visibility = TextView.VISIBLE
            txtTime.text = "Verifique seu email!"
        }
    }
}