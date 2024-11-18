package br.ecosynergy_app.home.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.home.HomeActivity
import br.ecosynergy_app.readings.ReadingsViewModel
import br.ecosynergy_app.readings.ReadingsViewModelFactory
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.invites.InvitesRepository
import br.ecosynergy_app.room.teams.MembersRepository
import br.ecosynergy_app.room.teams.TeamsRepository
import br.ecosynergy_app.room.user.UserRepository
import br.ecosynergy_app.teams.invites.InviteResponse
import br.ecosynergy_app.teams.viewmodel.ActivitiesResponseItem
import br.ecosynergy_app.teams.viewmodel.Link
import br.ecosynergy_app.teams.viewmodel.LinksResponse
import br.ecosynergy_app.teams.viewmodel.TeamsResponse
import br.ecosynergy_app.teams.viewmodel.TeamsViewModel
import br.ecosynergy_app.teams.viewmodel.TeamsViewModelFactory
import br.ecosynergy_app.user.viewmodel.UserViewModel
import br.ecosynergy_app.user.viewmodel.UserViewModelFactory
import com.google.android.material.button.MaterialButton
import de.hdodenhof.circleimageview.CircleImageView
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationActivity : AppCompatActivity() {
    private lateinit var userViewModel: UserViewModel
    private lateinit var teamsViewModel: TeamsViewModel

    private var accessToken: String = ""
    private var userId: Int = 0
    private var type: String = ""
    private var teamId: Int = 0
    private var inviteId: Int = 0

    private var time: String = ""

    private var inviteStatus: String = ""

    private lateinit var btnClose: ImageView

    private lateinit var linearFireAlert: LinearLayout
    private lateinit var linearGreetings: LinearLayout
    private lateinit var linearInviteStatus: LinearLayout
    private lateinit var linearInvite: LinearLayout
    private lateinit var inviteHandle: LinearLayout
    private lateinit var linearAccept: LinearLayout
    private lateinit var linearDecline: LinearLayout

    private lateinit var txtTeamName: TextView
    private lateinit var txtTeamDescription: TextView
    private lateinit var txtUserFullname: TextView
    private lateinit var imgUser: CircleImageView
    private lateinit var imgTeam: CircleImageView

    private lateinit var imgTeamStatus: CircleImageView

    private lateinit var txtStatus: TextView
    private lateinit var icStatus: ImageView
    private lateinit var linearStatus: LinearLayout

    private lateinit var txtStatusStatus: TextView
    private lateinit var txtDescriptionStatus: TextView
    private lateinit var txtTeamHandle: TextView

    private lateinit var imgSender: CircleImageView
    private lateinit var imgRecipient: CircleImageView
    private lateinit var imgStatus: ImageView

    private lateinit var imgInvite: ImageView

    private lateinit var btnFireTeamAccess: MaterialButton
    private lateinit var txtFireAlert: TextView

    private lateinit var btnSiteAccess: MaterialButton

    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        val userRepository = UserRepository(AppDatabase.getDatabase(applicationContext).userDao())
        val teamsRepository =
            TeamsRepository(AppDatabase.getDatabase(applicationContext).teamsDao())
        val membersRepository =
            MembersRepository(AppDatabase.getDatabase(applicationContext).membersDao())
        val invitesRepository =
            InvitesRepository(AppDatabase.getDatabase(applicationContext).invitesDao())

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(RetrofitClient.userService, userRepository)
        )[UserViewModel::class.java]

        teamsViewModel = ViewModelProvider(
            this,
            TeamsViewModelFactory(
                RetrofitClient.teamsService,
                teamsRepository,
                RetrofitClient.invitesService,
                membersRepository,
                invitesRepository
            )
        )[TeamsViewModel::class.java]

        time = intent.getStringExtra("TIME") ?: ""

        accessToken = intent.getStringExtra("ACCESS_TOKEN") ?: ""
        type = intent.getStringExtra("TYPE") ?: ""
        userId = intent.getIntExtra("USER_ID", 0)
        teamId = intent.getIntExtra("TEAM_ID", 0)
        inviteId = intent.getIntExtra("INVITE_ID", 0)

        btnClose = findViewById(R.id.btnClose)

        progressBar = findViewById(R.id.progressBar)

        imgInvite = findViewById(R.id.imgInvite)

        linearFireAlert = findViewById(R.id.linearFireAlert)
        linearGreetings = findViewById(R.id.linearGreetings)
        linearInviteStatus = findViewById(R.id.linearInviteStatus)
        linearInvite = findViewById(R.id.linearInvite)
        inviteHandle = findViewById(R.id.inviteHandle)
        linearAccept = findViewById(R.id.linearAccept)
        linearDecline = findViewById(R.id.linearDecline)

        txtTeamName = findViewById(R.id.txtTeamName)
        txtTeamDescription = findViewById(R.id.txtTeamDescription)
        txtUserFullname = findViewById(R.id.txtUserFullname)
        imgUser = findViewById(R.id.imgUser)
        imgTeam = findViewById(R.id.imgTeam)

        imgTeamStatus = findViewById(R.id.imgTeamStatus)

        txtStatus = findViewById(R.id.txtStatus)
        icStatus = findViewById(R.id.icStatus)
        linearStatus = findViewById(R.id.linearStatus)

        imgSender = findViewById(R.id.imgSender)
        imgRecipient = findViewById(R.id.imgRecipient)
        imgStatus = findViewById(R.id.imgStatus)

        txtStatusStatus = findViewById(R.id.txtStatusStatus)
        txtDescriptionStatus = findViewById(R.id.txtDescriptionStatus)
        txtTeamHandle = findViewById(R.id.txtTeamHandle)

        btnFireTeamAccess = findViewById(R.id.btnFireTeamAccess)
        txtFireAlert = findViewById(R.id.txtFireAlert)

        btnSiteAccess = findViewById(R.id.btnSiteAccess)

        btnClose.setOnClickListener { finish() }

        linearFireAlert.visibility = View.GONE
        linearInvite.visibility = View.GONE
        linearInviteStatus.visibility = View.GONE
        linearGreetings.visibility = View.GONE

        progressBar.visibility = View.VISIBLE

        when (type) {
            "fire" -> {
                handleFire()
            }
            "invite" -> {
                handleInvite()
            }

            else -> {}
        }
    }

    private fun handleInviteStatus() {
        teamsViewModel.findInviteById(inviteId, accessToken) {
            val inviteResult =
                teamsViewModel.inviteResult.value!!.body() ?: InviteResponse(0, 0, 0, 0, "", "", "")
            teamsViewModel.getTeamById(inviteResult.teamId, accessToken) {
                val teamResponse = teamsViewModel.teamResponse.value!!.body() ?: TeamsResponse(
                    0,
                    "",
                    "",
                    "",
                    ActivitiesResponseItem(0, "", ""),
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    "",
                    "",
                    "",
                    listOf(),
                    LinksResponse(Link(""))
                )
                userViewModel.getUserById(inviteResult.senderId, accessToken) {
                    userViewModel.user.value?.onSuccess { senderInfo ->
                        userViewModel.getUserById(inviteResult.recipientId, accessToken) {
                            userViewModel.user.value?.onSuccess { recipientInfo ->

                                txtTeamHandle.text = "@${teamResponse.handle}"
                                inviteStatus = inviteResult.status
                                imgTeamStatus.setImageResource(
                                    HomeActivity().getDrawableForLetter(
                                        teamResponse.name.first()
                                    )
                                )
                                imgSender.setImageResource(
                                    HomeActivity().getDrawableForLetter(
                                        senderInfo.fullName.first()
                                    )
                                )

                                imgRecipient.setImageResource(
                                    HomeActivity().getDrawableForLetter(
                                        recipientInfo.fullName.first()
                                    )
                                )

                                when (inviteStatus) {
                                    "ACCEPTED" -> {
                                        txtStatusStatus.text = "Seu convite foi aceito."
                                        txtDescriptionStatus.text =
                                            "Seu convite enviado para @${recipientInfo.username} foi aceito pelo destinatário."
                                    }

                                    "DECLINED" -> {
                                        txtStatusStatus.text = "Seu convite foi negado."
                                        txtDescriptionStatus.text =
                                            "Seu convite enviado para @${recipientInfo.username} foi negado pelo destinatário."
                                    }

                                    else -> txtStatusStatus.text = ""
                                }

                                imgStatus.setImageResource(
                                    when (inviteStatus) {
                                        "ACCEPTED" -> R.drawable.ic_successful
                                        "PENDING" -> R.drawable.ic_pending
                                        "DECLINED" -> R.drawable.ic_error
                                        else -> R.drawable.ic_pending
                                    }
                                )
                                imgInvite.visibility = View.VISIBLE
                                Log.d("NotificationActivity", "$inviteStatus")
                                setIconTint(inviteStatus)


                                linearInviteStatus.visibility = View.VISIBLE

                                progressBar.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleInvite() {
        teamsViewModel.findInviteById(inviteId, accessToken) {
            val inviteResult = teamsViewModel.inviteResult.value!!.body() ?: InviteResponse(
                0,
                0,
                0,
                0,
                "",
                "",
                ""
            )
            if (inviteResult.senderId == userId) {
                handleInviteStatus()
            } else {
                teamsViewModel.getTeamById(inviteResult.teamId, accessToken) {
                    val teamResponse = teamsViewModel.teamResponse.value!!.body() ?: TeamsResponse(
                        0,
                        "",
                        "",
                        "",
                        ActivitiesResponseItem(0, "", ""),
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        "",
                        "",
                        "",
                        listOf(),
                        LinksResponse(Link(""))
                    )
                    userViewModel.getUserById(inviteResult.senderId, accessToken) {
                        userViewModel.user.value?.onSuccess { senderInfo ->

                            txtTeamDescription.text = teamResponse.description
                            txtTeamName.text = teamResponse.name
                            txtUserFullname.text = senderInfo.fullName
                            inviteStatus = inviteResult.status
                            imgTeam.setImageResource(
                                HomeActivity().getDrawableForLetter(
                                    teamResponse.name.first()
                                )
                            )
                            imgUser.setImageResource(HomeActivity().getDrawableForLetter(senderInfo.fullName.first()))

                            when (inviteStatus) {
                                "ACCEPTED" -> txtStatus.text = "Este convite já foi aceito"
                                "DECLINED" -> txtStatus.text = "Este convite já foi negado"
                                "PENDING" -> txtStatus.text = "Este convite está pendente"
                                else -> txtStatus.text = ""
                            }

                            icStatus.setImageResource(
                                when (inviteStatus) {
                                    "ACCEPTED" -> R.drawable.ic_successful
                                    "PENDING" -> R.drawable.ic_pending
                                    "DECLINED" -> R.drawable.ic_error
                                    else -> R.drawable.ic_pending
                                }
                            )

                            setIconTint(inviteStatus)

                            if (inviteStatus == "ACCEPTED" || inviteStatus == "DECLINED") {

                                linearStatus.visibility = View.VISIBLE
                                inviteHandle.visibility = View.GONE
                                linearInvite.visibility = View.VISIBLE
                            } else {
                                inviteHandle.visibility = View.VISIBLE
                                linearInvite.visibility = View.VISIBLE
                            }

                            progressBar.visibility = View.GONE
                        }
                    }
                }
            }

            linearAccept.setOnClickListener {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Você deseja aceitar o convite?")
                builder.setMessage("Você será adicionado a essa equipe como membro.")


                builder.setPositiveButton("Sim") { dialog, _ ->
                    progressBar.visibility = View.VISIBLE
                    teamsViewModel.acceptInvite(inviteId, accessToken) {
                        progressBar.visibility = View.GONE
                        finish()
                    }
                }

                builder.setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }

                val dialog: AlertDialog = builder.create()
                dialog.show()
            }

            linearDecline.setOnClickListener {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Você deseja negar o convite?")
                builder.setMessage("Este convite não poderá ser aceito outra hora")

                builder.setPositiveButton("Sim") { dialog, _ ->
                    progressBar.visibility = View.VISIBLE
                    teamsViewModel.declineInvite(inviteId, accessToken) {
                        progressBar.visibility = View.GONE
                        finish()
                    }
                }

                builder.setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }

                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }
    }

    private fun handleFire() {
            teamsViewModel.getTeamById(teamId, accessToken) {
                val teamResponse = teamsViewModel.teamResponse.value!!.body() ?: TeamsResponse(
                    0,
                    "",
                    "",
                    "",
                    ActivitiesResponseItem(0, "", ""),
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    "",
                    "",
                    "",
                    listOf(),
                    LinksResponse(Link(""))
                )

                val date = formatDate(time)
                txtFireAlert.text = "Houve uma detecção de emissão de fogo na equipe @${teamResponse.handle} às $date"
                linearFireAlert.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
        }
    }

    private fun formatDate(input: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm 'de' dd/MM", Locale.getDefault())

        val date = inputFormat.parse(input)
        return if (date != null) {
            outputFormat.format(date)
        } else {
            "Invalid date"
        }
    }

    private fun setIconTint(status: String) {
        val color = when (status) {
            "ACCEPTED" -> getColor(R.color.greenDark)
            "PENDING" -> getThemeColor(android.R.attr.colorAccent, R.color.gray)
            "DECLINED" -> getColor(R.color.red)
            else -> getColor(R.color.black)
        }
        icStatus.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
        imgStatus.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
    }

    private fun getThemeColor(attrResId: Int, defaultColorRes: Int): Int {
        val typedValue = TypedValue()
        val theme = theme
        return if (theme.resolveAttribute(attrResId, typedValue, true)) {
            typedValue.data
        } else {
            getColor(defaultColorRes)
        }
    }
}
