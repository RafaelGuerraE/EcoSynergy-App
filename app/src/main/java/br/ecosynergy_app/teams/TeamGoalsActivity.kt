package br.ecosynergy_app.teams

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.teams.MembersRepository
import br.ecosynergy_app.room.teams.TeamsRepository
import br.ecosynergy_app.teams.viewmodel.TeamsViewModel
import br.ecosynergy_app.teams.viewmodel.TeamsViewModelFactory

class TeamGoalsActivity : AppCompatActivity() {

    private lateinit var teamsViewModel: TeamsViewModel

    private lateinit var btnBack : ImageButton

    private lateinit var txtDailyGoal: TextView
    private lateinit var txtWeeklyGoal: TextView
    private lateinit var txtMonthlyGoal: TextView
    private lateinit var txtAnnualGoal: TextView

    private var dailyGoal: Double = 0.0
    private var weeklyGoal: Double = 0.0
    private var monthlyGoal: Double = 0.0
    private var annualGoal: Double = 0.0

    private var dailyGoalText: String = ""
    private var weeklyGoalText: String = ""
    private var monthlyGoalText: String = ""
    private var annualGoalText: String = ""

    private lateinit var btnGoals: LinearLayout

    private var userId: Int = 0
    private var userRole: String = ""
    private var accessToken: String = ""
    private var teamName: String = ""
    private var teamId: Int = 0
    private var teamHandle: String = ""
    private var measure: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_goals)

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
            val editTeamGoalsBottomSheet = EditTeamGoalsBottomSheet().apply {
                arguments = Bundle().apply {
                    putString("TEAM_HANDLE", teamHandle)
                    putInt("TEAM_ID", teamId)
                    putInt("USER_ID", userId)
                    putString("ACCESS_TOKEN", accessToken)
                    putDouble("dailyGoal", dailyGoal)
                    putDouble("weeklyGoal", weeklyGoal)
                    putDouble("monthlyGoal", monthlyGoal)
                    putDouble("annualGoal", annualGoal)
                }
            }
            editTeamGoalsBottomSheet.show(supportFragmentManager, "GoalsBottomSheet")
        }

    }

    override fun onResume() {
        super.onResume()

        observeTeamInfo()
    }

    private fun observeTeamInfo() {
        teamsViewModel.getTeamById(teamId)
        teamsViewModel.teamDB.observe(this) { teamInfo ->

            measure = " toneladas"

            teamId = teamInfo.id
            teamName = teamInfo.name

            dailyGoal = teamInfo.dailyGoal
            weeklyGoal = teamInfo.weeklyGoal
            monthlyGoal = teamInfo.monthlyGoal
            annualGoal = teamInfo.annualGoal

            dailyGoalText = formatGoal(dailyGoal)
            weeklyGoalText = formatGoal(weeklyGoal)
            monthlyGoalText = formatGoal(monthlyGoal)
            annualGoalText = formatGoal(annualGoal)

            txtDailyGoal.text = dailyGoalText + measure
            txtWeeklyGoal.text = weeklyGoalText + measure
            txtMonthlyGoal.text = monthlyGoalText + measure
            txtAnnualGoal.text = annualGoalText + measure
        }
    }

    private fun formatGoal(goal: Double): String {
        return when {
            goal < 1000 -> goal.toInt().toString()
            goal < 1_000_000 -> "${(goal / 1000).toInt()} mil"
            goal < 1_000_000_000 -> "${(goal / 1_000_000).toInt()} milhões"
            else -> "${(goal / 1_000_000_000).toInt()} bilhões"
        }
    }
}