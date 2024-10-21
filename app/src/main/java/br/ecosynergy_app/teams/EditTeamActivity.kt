package br.ecosynergy_app.teams

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.login.LoginActivity
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.teams.MembersRepository
import br.ecosynergy_app.room.teams.TeamsRepository
import br.ecosynergy_app.room.user.UserRepository
import br.ecosynergy_app.teams.viewmodel.ActivitiesRequest
import br.ecosynergy_app.teams.viewmodel.Sector
import br.ecosynergy_app.teams.viewmodel.TeamsViewModel
import br.ecosynergy_app.teams.viewmodel.TeamsViewModelFactory
import br.ecosynergy_app.teams.viewmodel.Timezone
import br.ecosynergy_app.teams.viewmodel.UpdateRequest
import br.ecosynergy_app.user.UserViewModel
import br.ecosynergy_app.user.UserViewModelFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class EditTeamActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var teamsViewModel: TeamsViewModel

    private lateinit var txtTeamName: EditText
    private lateinit var txtHandle: EditText
    private lateinit var txtDescription: EditText
    private lateinit var txtTimezone: AutoCompleteTextView
    private lateinit var spinnerActivities: Spinner

    private lateinit var btnBack: ImageButton

    private lateinit var btnConfirm: ImageButton
    private lateinit var checkProgress: ProgressBar

    private var accessToken: String = ""

    private var timezone: String = ""
    private var selectedTimezoneText = ""

    private var utcToTextMap: Map<String?, String> = mapOf()
    private var timezonesMap: Map<String?, String?> = mapOf()

    private var teamId: Int = 0
    private var teamName: String = ""
    private var teamHandle: String = ""
    private var teamDescription: String = ""
    private var teamActivity: String = ""
    private var teamTimezone: String = ""

    private var dailyGoal: String = ""
    private var weeklyGoal: String = ""
    private var monthlyGoal: String = ""
    private var annualGoal: String = ""

    private var activitySelected: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_team)

        val userDao = AppDatabase.getDatabase(this).userDao()
        val userRepository = UserRepository(userDao)

        val teamsDao = AppDatabase.getDatabase(this).teamsDao()
        val teamsRepository = TeamsRepository(teamsDao)

        val membersDao = AppDatabase.getDatabase(this).membersDao()
        val membersRepository = MembersRepository(membersDao)

        teamsViewModel = ViewModelProvider(
            this,
            TeamsViewModelFactory(RetrofitClient.teamsService, teamsRepository, membersRepository)
        )[TeamsViewModel::class.java]
        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(RetrofitClient.userService, userRepository)
        )[UserViewModel::class.java]

        txtTeamName = findViewById(R.id.txtTeamName)
        txtHandle = findViewById(R.id.txtHandle)
        txtTimezone = findViewById(R.id.txtTimezone)
        txtDescription = findViewById(R.id.txtDescription)
        spinnerActivities = findViewById(R.id.spinnerActivities)

        btnConfirm = findViewById(R.id.btnConfirm)
        checkProgress = findViewById(R.id.checkProgress)

        btnBack = findViewById(R.id.btnBack)


        val timezones = loadTimezones()
        timezonesMap = timezones.associate { it.text to it.utc.firstOrNull() }
        utcToTextMap = timezones.associate { it.utc.firstOrNull() to it.text }
        val timezoneText = timezones.map { it.text }
        val timezoneAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, timezoneText)
        txtTimezone.setAdapter(timezoneAdapter)

        txtTimezone.setOnItemClickListener { parent, _, position, _ ->
            selectedTimezoneText = parent.getItemAtPosition(position) as String
            val selectedTimezoneUtc = timezonesMap[selectedTimezoneText]
            timezone = selectedTimezoneUtc.toString()
        }

        accessToken = intent.getStringExtra("accessToken") ?: ""
        teamName = intent.getStringExtra("teamName") ?: ""
        teamId = intent.getIntExtra("teamId", 0)
        teamHandle = intent.getStringExtra("teamHandle") ?: ""
        teamDescription = intent.getStringExtra("teamDescription") ?: ""
        teamActivity = intent.getStringExtra("teamSector") ?: ""
        teamTimezone = intent.getStringExtra("teamTimezone") ?: ""
        dailyGoal = intent.getStringExtra("dailyGoal") ?: ""
        weeklyGoal = intent.getStringExtra("weeklyGoal") ?: ""
        monthlyGoal = intent.getStringExtra("monthlyGoal") ?: ""
        annualGoal = intent.getStringExtra("annualGoal") ?: ""

        txtTeamName.setText(teamName)
        txtHandle.setText(teamHandle)
        txtDescription.setText(teamDescription)
        txtTimezone.setText(teamTimezone)

        setupSpinner()

        btnBack.setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra("teamName", teamName)
                putExtra("teamHandle", teamHandle)
                putExtra("teamDescription", teamDescription)
                putExtra("teamTimezone", txtTimezone.text.toString())
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }


        btnConfirm.setOnClickListener {

            teamHandle = txtHandle.text.toString()
            teamName = txtTeamName.text.toString()
            teamDescription = txtDescription.text.toString()
            teamTimezone = txtTimezone.text.toString()
            teamDescription = txtDescription.text.toString()
            teamTimezone = timezone
            teamName = txtTeamName.text.toString()


            editTeamInfo()
        }

    }

    private fun editTeamInfo() {
        disableEditTexts()
        btnConfirm.visibility = View.GONE
        checkProgress.visibility = View.VISIBLE

        teamsViewModel.teamResult.removeObservers(this)
        teamsViewModel.updateTeam(
            accessToken,
            teamId,
            UpdateRequest(
                teamHandle,
                teamName,
                teamDescription,
                ActivitiesRequest(activitySelected),
                dailyGoal.toDouble(),
                weeklyGoal.toDouble(),
                monthlyGoal.toDouble(),
                annualGoal.toDouble(),
                teamTimezone
            )
        )

        teamsViewModel.updateResponse.observe(this) { result ->
            result.onSuccess {
                showToast("Informações editadas com sucesso!")
                btnConfirm.visibility = View.VISIBLE
                checkProgress.visibility = View.GONE
                enableEditTexts()
            }
            result.onFailure {
                showToast("Erro")
                btnConfirm.visibility = View.VISIBLE
                checkProgress.visibility = View.GONE
                enableEditTexts()
            }
        }
    }

    private fun getThemeColor(attrResId: Int): Int {
        val typedValue = TypedValue()
        val theme = this.theme
        theme.resolveAttribute(attrResId, typedValue, true)
        return typedValue.data
    }

    private fun enableEditTexts() {
        txtTimezone.isEnabled = true
        txtHandle.isEnabled = true
        txtDescription.isEnabled = true
        txtTeamName.isEnabled = true
        spinnerActivities.isEnabled = true

        txtTimezone.setTextColor(getThemeColor(android.R.attr.textColorPrimary))
        txtHandle.setTextColor(getThemeColor(android.R.attr.textColorPrimary))
        txtDescription.setTextColor(getThemeColor(android.R.attr.textColorPrimary))
        txtTeamName.setTextColor(getThemeColor(android.R.attr.textColorPrimary))
    }

    private fun disableEditTexts() {
        txtTimezone.isEnabled = false
        txtHandle.isEnabled = false
        txtDescription.isEnabled = false
        txtTeamName.isEnabled = false
        spinnerActivities.isEnabled = false


        txtTimezone.setTextColor(ContextCompat.getColor(this, R.color.disabled))
        txtHandle.setTextColor(ContextCompat.getColor(this, R.color.disabled))
        txtDescription.setTextColor(ContextCompat.getColor(this, R.color.disabled))
        txtTeamName.setTextColor(ContextCompat.getColor(this, R.color.disabled))
    }

    private fun setupSpinner() {
        val sectors = loadSectorsAndActivities()
        val spinnerItems = mutableListOf<String>()
        val activityMap = mutableMapOf<Int, String>()

        for (sector in sectors) {
            for (activity in sector.activities) {
                val spinnerItem = "${sector.sector_br}/${activity.activities_br}"
                spinnerItems.add(spinnerItem)
                activityMap[activity.activities_id] = spinnerItem // Map ID to the full string
            }
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerActivities.adapter = adapter

        setSpinnerSelection(teamActivity, spinnerItems)

        spinnerActivities.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedActivity = parent.getItemAtPosition(position) as String
                activitySelected = activityMap.keys.first { activityMap[it] == selectedActivity }
                Log.d("EditTeamActivity", "$activitySelected")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setSpinnerSelection(teamActivity: String, spinnerItems: List<String>) {
        if (teamActivity.isNotEmpty()) {
            val position = spinnerItems.indexOfFirst { it == teamActivity }
            Log.d("EditTeamActivity", "Position: $position")
            if (position >= 0) {
                spinnerActivities.setSelection(position)
            } else {
                Log.e("EditTeamActivity", "Team activity not found in spinner: $teamActivity")
            }
        }
    }


    private fun loadTimezones(): List<Timezone> {
        val jsonString = getJson("timezones.json")
        val gson = Gson()
        val listType = object : TypeToken<List<Timezone>>() {}.type
        return gson.fromJson(jsonString, listType)
    }

    private fun loadSectorsAndActivities(): List<Sector> {
        val jsonFileString = getJson("sectors.json")
        val gson = Gson()
        val listSectorType = object : TypeToken<List<Sector>>() {}.type
        return gson.fromJson(jsonFileString, listSectorType)
    }

    private fun getJson(fileName: String): String? {
        return try {
            val inputStream = assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}