package br.ecosynergy_app.home

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import br.ecosynergy_app.R

class UserSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usersettings)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnEditUsername = findViewById<ImageButton>(R.id.btnEditUsername)
        val btnEditFullname = findViewById<ImageButton>(R.id.btnEditFullname)
        val btnEditGender = findViewById<ImageButton>(R.id.btnEditGender)
        val btnEditNationality = findViewById<ImageButton>(R.id.btnEditNationality)
        val btnEditPassword = findViewById<ImageButton>(R.id.btnEditPassword)


        btnBack.setOnClickListener(){
            finish()
        }

    }
}
