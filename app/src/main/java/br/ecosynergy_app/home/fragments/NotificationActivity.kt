package br.ecosynergy_app.home.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import br.ecosynergy_app.R
import com.google.android.material.button.MaterialButton
import de.hdodenhof.circleimageview.CircleImageView

class NotificationActivity : AppCompatActivity() {

    private var type: String? = null
    private var teamId: Int? = null
    private var inviteId: Int? = null

    private lateinit var btnClose: ImageView

    private lateinit var linearFireAlert: LinearLayout
    private lateinit var linearGreetings: LinearLayout
    private lateinit var linearInviteStatus: LinearLayout
    private lateinit var linearInvite: LinearLayout
    private lateinit var inviteHandle: LinearLayout
    private lateinit var linearAccept: LinearLayout
    private lateinit var linearDecline: LinearLayout

    private lateinit var txtUserFullname: TextView
    private lateinit var imgUser: CircleImageView
    private lateinit var imgTeam: CircleImageView

    private lateinit var imgSender: CircleImageView
    private lateinit var imgRecipient: CircleImageView
    private lateinit var imgStatus: ImageView

    private lateinit var btnFireTeamAccess: MaterialButton
    private lateinit var txtFireAlert: TextView

    private lateinit var btnSiteAccess: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        type = intent.getStringExtra("TYPE")
        teamId = intent.getIntExtra("TEAM_ID", 0)
        inviteId = intent.getIntExtra("INVITE_ID", 0)

        btnClose = findViewById(R.id.btnClose)

        linearFireAlert = findViewById(R.id.linearFireAlert)
        linearGreetings = findViewById(R.id.linearGreetings)
        linearInviteStatus = findViewById(R.id.linearInviteStatus)
        linearInvite = findViewById(R.id.linearInvite)
        inviteHandle = findViewById(R.id.inviteHandle)
        linearAccept = findViewById(R.id.linearAccept)
        linearDecline = findViewById(R.id.linearDecline)

        txtUserFullname = findViewById(R.id.txtUserFullname)
        imgUser = findViewById(R.id.imgUser)
        imgTeam = findViewById(R.id.imgTeam)

        imgSender = findViewById(R.id.imgSender)
        imgRecipient = findViewById(R.id.imgRecipient)
        imgStatus = findViewById(R.id.imgStatus)

        btnFireTeamAccess = findViewById(R.id.btnFireTeamAccess)
        txtFireAlert = findViewById(R.id.txtFireAlert)

        btnSiteAccess = findViewById(R.id.btnSiteAccess)

        btnClose.setOnClickListener { finish() }


        linearFireAlert.visibility = View.GONE
        linearInvite.visibility = View.GONE
        linearInviteStatus.visibility = View.GONE
        linearGreetings.visibility = View.GONE

        when (type) {
            "fire" -> {
                linearFireAlert.visibility = View.VISIBLE
            }
            "invite" -> {
                inviteHandle.visibility = View.VISIBLE
                linearInvite.visibility = View.VISIBLE
            }
            "greeting" -> {
                linearGreetings.visibility = View.VISIBLE
            }
            else -> {
            }
        }

    }
}
