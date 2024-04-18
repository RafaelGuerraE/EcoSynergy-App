package br.ecosynergy_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class ConfirmationActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)

        val btncheck: Button = findViewById(R.id.btncheck)

        btncheck.setOnClickListener(){
            val i = Intent(this, HomeActivity::class.java)
            startActivity(i)
        }
    }
}