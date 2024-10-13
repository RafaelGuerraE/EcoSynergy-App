package br.ecosynergy_app.teams

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import br.ecosynergy_app.R
import de.hdodenhof.circleimageview.CircleImageView

class DashboardActivity : AppCompatActivity() {

    private lateinit var btnClose : ImageButton
    private lateinit var lblTeamName: TextView
    private lateinit var imgTeam: CircleImageView

    private var teamHandle: String? = null
    private var teamInitial: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        btnClose = findViewById(R.id.btnClose)
        lblTeamName = findViewById(R.id.lblTeamName)
        imgTeam = findViewById(R.id.imgTeam)

        teamInitial = intent.getIntExtra("TEAM_INITIAL", 0)
        teamHandle = intent.getStringExtra("TEAM_HANDLE").toString()

        imgTeam.setImageResource(teamInitial)
        lblTeamName.text = teamHandle

        btnClose.setOnClickListener{ finish() }
    }
}