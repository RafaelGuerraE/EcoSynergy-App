package br.ecosynergy_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnlogin: Button = findViewById(R.id.btnlogin)
        val txtemail: EditText = findViewById(R.id.txtemail)
        val txtpassword: EditText = findViewById(R.id.txtpassword)
        val btnregister: Button = findViewById(R.id.btnregister)

        btnlogin.setOnClickListener(){
            val intent: Intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        btnregister.setOnClickListener(){
            val intent: Intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}