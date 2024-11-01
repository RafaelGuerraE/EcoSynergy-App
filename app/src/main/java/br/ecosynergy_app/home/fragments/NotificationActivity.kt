package br.ecosynergy_app.home.fragments

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import br.ecosynergy_app.R

class NotificationActivity : AppCompatActivity() {

    private lateinit var btnClose: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        btnClose = findViewById(R.id.btnClose)

        btnClose.setOnClickListener{ finish() }


    }
}