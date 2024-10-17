package br.ecosynergy_app.teams

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.ecosynergy_app.R

class InvitesActivity : AppCompatActivity() {

    private lateinit var btnBack : ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invites)

        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener{ finish() }
    }
}