package br.ecosynergy_app.teams

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import br.ecosynergy_app.R

class TeamInvitesActivity : AppCompatActivity() {

    private lateinit var btnBack : ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_invites)

        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener{ finish() }
    }
}