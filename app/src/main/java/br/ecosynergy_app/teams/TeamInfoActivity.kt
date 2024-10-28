package br.ecosynergy_app.teams

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.invites.InvitesRepository
import br.ecosynergy_app.room.teams.MembersRepository
import br.ecosynergy_app.room.teams.TeamsRepository
import br.ecosynergy_app.room.user.UserRepository
import br.ecosynergy_app.teams.viewmodel.TeamsViewModel
import br.ecosynergy_app.teams.viewmodel.TeamsViewModelFactory
import br.ecosynergy_app.user.viewmodel.UserViewModel
import br.ecosynergy_app.user.viewmodel.UserViewModelFactory
import de.hdodenhof.circleimageview.CircleImageView

class TeamInfoActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var teamsViewModel: TeamsViewModel

    private var userId: Int = 0
    private var accessToken: String = ""
    private var teamId: Int = 0
    private var teamHandle: String = ""
    private var userRole: String = ""
    private var teamInitial: Int = 0

    private lateinit var teamOverviewLauncher: ActivityResultLauncher<Intent>
    private lateinit var teamMembersLauncher: ActivityResultLauncher<Intent>

    private val userRepository = UserRepository(AppDatabase.getDatabase(this).userDao())
    private val teamsRepository = TeamsRepository(AppDatabase.getDatabase(this).teamsDao())
    private val membersRepository = MembersRepository(AppDatabase.getDatabase(this).membersDao())
    private val invitesRepository = InvitesRepository(AppDatabase.getDatabase(this).invitesDao())

    private lateinit var btnClose: ImageButton
    private lateinit var imgTeam: CircleImageView
    private lateinit var areaOverview: LinearLayout
    private lateinit var areaGoals: LinearLayout
    private lateinit var areaMembers: LinearLayout
    private lateinit var areaInvites: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_info)

        teamsViewModel = ViewModelProvider(
            this,
            TeamsViewModelFactory(RetrofitClient.teamsService, teamsRepository, RetrofitClient.invitesService, membersRepository, invitesRepository)
        )[TeamsViewModel::class.java]
        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(RetrofitClient.userService, userRepository)
        )[UserViewModel::class.java]

        teamId = intent.getIntExtra("TEAM_ID", 0)
        teamHandle = intent.getStringExtra("TEAM_HANDLE").toString()
        teamInitial = intent.getIntExtra("TEAM_INITIAL", 0)

        teamOverviewLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val deletedTeamId = result.data?.getIntExtra("TEAM_ID", -1) ?: -1
                    if (deletedTeamId != -1) {
                        setResult(RESULT_OK, Intent().apply {
                            putExtra("TEAM_ID", deletedTeamId)
                        })
                        finish()
                    }
                }
            }

        teamMembersLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val exitedTeamId = result.data?.getIntExtra("TEAM_ID", -1) ?: -1
                    if (exitedTeamId != -1) {
                        setResult(RESULT_OK, Intent().apply {
                            putExtra("TEAM_ID", exitedTeamId)
                        })
                        finish()
                    }
                }
            }


        btnClose = findViewById(R.id.btnClose)
        imgTeam = findViewById(R.id.imgTeam)
        areaOverview = findViewById(R.id.areaOverview)
        areaGoals = findViewById(R.id.areaGoals)
        areaMembers = findViewById(R.id.areaMembers)
        areaInvites = findViewById(R.id.areaInvites)

        btnClose.setOnClickListener { finish() }

        imgTeam.setImageResource(teamInitial)

        areaOverview.setOnClickListener {
            val i = Intent(this, TeamOverviewActivity::class.java).apply {
                putExtra("TEAM_ID", teamId)
                putExtra("TEAM_HANDLE", teamHandle)
                putExtra("ACCESS_TOKEN", accessToken)
                putExtra("USER_ROLE", userRole)
            }
            teamOverviewLauncher.launch(i)
        }


        areaGoals.setOnClickListener {
            val i = Intent(this, TeamGoalsActivity::class.java).apply {
                putExtra("TEAM_ID", teamId)
                putExtra("TEAM_HANDLE", teamHandle)
                putExtra("ACCESS_TOKEN", accessToken)
                putExtra("USER_ROLE", userRole)
            }
            startActivity(i)
        }

        areaMembers.setOnClickListener {
            val i = Intent(this, TeamMembersActivity::class.java).apply {
                putExtra("TEAM_ID", teamId)
                putExtra("TEAM_HANDLE", teamHandle)
                putExtra("USER_ID", userId)
                putExtra("ACCESS_TOKEN", accessToken)
                putExtra("USER_ROLE", userRole)
            }
            teamMembersLauncher.launch(i)
        }

        areaInvites.setOnClickListener {

            if(userRole == "ADMINISTRATOR" || userRole == "FOUNDER") {
                val i = Intent(this, TeamInvitesActivity::class.java).apply {
                    putExtra("TEAM_ID", teamId)
                    putExtra("TEAM_HANDLE", teamHandle)
                    putExtra("ACCESS_TOKEN", accessToken)
                }
                startActivity(i)
            }
            else{
                showToast("Você não pode acessar os convites da equipe")
            }
        }


        observeUserData {
            observeMembersInfo()
        }
    }

    private fun observeMembersInfo() {
        teamsViewModel.getMembersByTeamId(teamId)
        teamsViewModel.allMembersDB.observe(this) { membersInfo ->
            val userMember = membersInfo.find { it.userId == userId }
            userRole = userMember?.role.toString()
            Log.d("TeamInfoActivity", "UserRole: $userRole")
        }
    }

    private fun observeUserData(onComplete: () -> Unit) {
        userViewModel.getUserInfoFromDB {}
        userViewModel.userInfo.observe(this) { userInfo ->
            userId = userInfo.id
            accessToken = userInfo.accessToken
            onComplete()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}