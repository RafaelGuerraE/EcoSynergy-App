package br.ecosynergy_app.home

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import br.ecosynergy_app.R

class UserSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usersettings)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        btnBack.setOnClickListener(){
            finish()
        }

    }
}
