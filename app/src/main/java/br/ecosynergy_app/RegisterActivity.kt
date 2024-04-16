package br.ecosynergy_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnregister: Button = findViewById(R.id.btnregister)

        btnregister.setOnClickListener(){
            val intent = Intent(this, ConfirmationActivity::class.java)
            startActivity(intent)
        }
    }
}