package br.ecosynergy_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class RegisterActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity2_register)

        val btnregister: Button = findViewById(R.id.btnregister)

        btnregister.setOnClickListener(){
            val i = Intent(this, ConfirmationActivity::class.java)
            startActivity(i)
        }
    }
}