package br.ecosynergy_app.teams

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.ecosynergy_app.R

class TeamActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team)

        val btnTeamSettings: ImageButton = findViewById(R.id.btnTeamSettings)

        btnTeamSettings.setOnClickListener {
            val i = Intent(this, TeamSettingsActivity::class.java)
            startActivity(i)
        }
    }
}