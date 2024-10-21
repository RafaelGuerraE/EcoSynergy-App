package br.ecosynergy_app.teams

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.home.HomeActivity
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.teams.MembersRepository
import br.ecosynergy_app.room.teams.TeamsRepository
import br.ecosynergy_app.teams.viewmodel.TeamsViewModel
import br.ecosynergy_app.teams.viewmodel.TeamsViewModelFactory

class GoalsActivity : AppCompatActivity() {

    private lateinit var teamsViewModel: TeamsViewModel

    private lateinit var btnBack : ImageButton
    private lateinit var txtDailyGoal: TextView
    private lateinit var txtWeeklyGoal: TextView
    private lateinit var txtMonthlyGoal: TextView
    private lateinit var txtAnnualGoal: TextView
    private lateinit var btnGoals: LinearLayout

    private var userId: Int = 0
    private var userRole: String = ""
    private var accessToken: String = ""
    private var teamName: String = ""
    private var teamId: Int = 0
    private var teamHandle: String = ""
    private var measure: String = " toneladas"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        val teamsDao = AppDatabase.getDatabase(this).teamsDao()
        val teamsRepository = TeamsRepository(teamsDao)

        val membersDao = AppDatabase.getDatabase(this).membersDao()
        val membersRepository = MembersRepository(membersDao)

        teamsViewModel = ViewModelProvider(
            this,
            TeamsViewModelFactory(RetrofitClient.teamsService, teamsRepository, membersRepository)
        )[TeamsViewModel::class.java]

        btnBack = findViewById(R.id.btnBack)
        txtDailyGoal = findViewById(R.id.txtDailyGoal)
        txtWeeklyGoal = findViewById(R.id.txtWeeklyGoal)
        txtMonthlyGoal = findViewById(R.id.txtMonthlyGoal)
        txtAnnualGoal = findViewById(R.id.txtAnnualGoal)
        btnGoals = findViewById(R.id.btnGoals)


        teamId = intent.getIntExtra("TEAM_ID", 0)
        teamHandle = intent.getStringExtra("TEAM_HANDLE").toString()
        userId = intent.getIntExtra("USER_ID", 0)
        accessToken = intent.getStringExtra("ACCESS_TOKEN").toString()
        userRole = intent.getStringExtra("USER_ROLE").toString()

        btnGoals.visibility = if(userRole == "ADMINISTRATOR") View.VISIBLE else View.GONE

        btnBack.setOnClickListener{ finish() }

        btnGoals.setOnClickListener {
            val goalsBottomSheet = GoalsBottomSheet().apply {
                arguments = Bundle().apply {
                    putString("TEAM_HANDLE", teamHandle)
                    putInt("TEAM_ID", teamId)
                    putInt("USER_ID", userId)
                    putString("ACCESS_TOKEN", accessToken)
                    putString("dailyGoal", txtDailyGoal.text.toString())
                    putString("weeklyGoal", txtWeeklyGoal.text.toString())
                    putString("monthlyGoal", txtMonthlyGoal.text.toString())
                    putString("annualGoal", txtAnnualGoal.text.toString())
                }
            }
            goalsBottomSheet.show(supportFragmentManager, "GoalsBottomSheet")
        }

        observeTeamInfo()
    }

    private fun observeTeamInfo() {
        teamsViewModel.getTeamById(teamId)
        teamsViewModel.teamDB.observe(this) { teamInfo ->

            teamId = teamInfo.id
            teamName = teamInfo.name


            txtDailyGoal.text = teamInfo.dailyGoal.toInt().toString() + measure
            txtWeeklyGoal.text = teamInfo.weeklyGoal.toInt().toString() + measure
            txtMonthlyGoal.text = teamInfo.monthlyGoal.toInt().toString() + measure
            txtAnnualGoal.text = teamInfo.annualGoal.toInt().toString() + measure

        }
    }
}