package br.ecosynergy_app.teams

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import br.ecosynergy_app.R
import br.ecosynergy_app.register.ConfirmationActivity
import de.hdodenhof.circleimageview.CircleImageView

class TeamSettingsActivity : AppCompatActivity() {

    lateinit var teamPicture: CircleImageView
    lateinit var lblTeamName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teamsettings)

        teamPicture = findViewById(R.id.teamPicture)
        lblTeamName = findViewById(R.id.lblTeamName)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnOverview = findViewById<Button>(R.id.btnOverview)
        val btnInvites = findViewById<Button>(R.id.btnInvites)
        val btnMembers = findViewById<Button>(R.id.btnMembers)
        val btnBans = findViewById<Button>(R.id.btnBans)

        btnOverview.setOnClickListener(){
            val i = Intent(this, TeamOverviewActivity::class.java)
            startActivity(i)
        }

        btnInvites.setOnClickListener(){
            val i = Intent(this, TeamInviteActivity::class.java)
            startActivity(i)
        }

        btnMembers.setOnClickListener(){
            val i = Intent(this, TeamMembersActivity::class.java)
            startActivity(i)
        }

        btnBans.setOnClickListener(){
            val i = Intent(this, TeamBansActivity::class.java)
            startActivity(i)
        }

        btnBack.setOnClickListener(){
            finish()
        }
    }
}