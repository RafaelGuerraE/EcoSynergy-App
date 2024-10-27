package br.ecosynergy_app.teams

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.invites.InvitesRepository
import br.ecosynergy_app.room.teams.MembersRepository
import br.ecosynergy_app.room.teams.TeamsRepository
import br.ecosynergy_app.teams.viewmodel.ActivitiesRequest
import br.ecosynergy_app.teams.viewmodel.Member
import br.ecosynergy_app.teams.viewmodel.Sector
import br.ecosynergy_app.teams.viewmodel.TeamsRequest
import br.ecosynergy_app.teams.viewmodel.TeamsViewModel
import br.ecosynergy_app.teams.viewmodel.TeamsViewModelFactory
import br.ecosynergy_app.teams.viewmodel.Timezone
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class CreateTeamActivity : AppCompatActivity() {

    private lateinit var teamsViewModel: TeamsViewModel

    private var timezone: String = ""

    private lateinit var btnClose: ImageButton
    private lateinit var txtHandle: EditText
    private lateinit var txtTeamName: EditText
    private lateinit var txtDescription: EditText
    private lateinit var spinnerActivities: Spinner
    private lateinit var btnCreateTeam: Button
    private lateinit var txtTimezone: AutoCompleteTextView
    private lateinit var txtDailyGoal: EditText
    private lateinit var txtWeeklyGoal: EditText
    private lateinit var txtMonthlyGoal: EditText
    private lateinit var txtAnnualGoal: EditText

    private lateinit var btnInfo: ImageButton
    private lateinit var btnEditAll: LinearLayout

    private lateinit var btnStepBack: TextView

    private lateinit var step2: TextView
    private lateinit var midStep1: View

    private lateinit var linearInfo: LinearLayout
    private lateinit var linearGoals: LinearLayout

    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var overlayView: View

    private var accessToken: String = ""
    private var userId: Int = 0

    private var step: Int = 0

    private var activitySelected: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_team)

        val teamsDao = AppDatabase.getDatabase(this).teamsDao()
        val teamsRepository = TeamsRepository(teamsDao)

        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        overlayView = findViewById(R.id.overlayView)

        btnClose = findViewById(R.id.btnClose)

        val membersRepository = MembersRepository(AppDatabase.getDatabase(this).membersDao())
        val invitesRepository = InvitesRepository(AppDatabase.getDatabase(applicationContext).invitesDao())

        teamsViewModel = ViewModelProvider(
            this,
            TeamsViewModelFactory(RetrofitClient.teamsService, teamsRepository, RetrofitClient.invitesService, membersRepository, invitesRepository)
        )[TeamsViewModel::class.java]

        txtHandle = findViewById(R.id.txtHandle)
        txtTeamName = findViewById(R.id.txtTeamName)
        txtDescription = findViewById(R.id.txtDescription)
        spinnerActivities = findViewById(R.id.spinnerActivities)
        btnCreateTeam = findViewById(R.id.btnCreateTeam)
        txtTimezone = findViewById(R.id.txtTimezone)
        txtDailyGoal = findViewById(R.id.txtDailyGoal)
        txtWeeklyGoal = findViewById(R.id.txtWeeklyGoal)
        txtMonthlyGoal = findViewById(R.id.txtMonthlyGoal)
        txtAnnualGoal = findViewById(R.id.txtAnnualGoal)

        btnEditAll = findViewById(R.id.btnEditAll)
        btnInfo = findViewById(R.id.btnInfo)

        step2 = findViewById(R.id.step2)
        midStep1 = findViewById(R.id.midStep1)

        btnStepBack = findViewById(R.id.btnStepBack)

        linearInfo = findViewById(R.id.linearInfo)
        linearGoals = findViewById(R.id.linearGoals)

        userId = intent.getIntExtra("USER_ID", 0)
        accessToken = intent.getStringExtra("ACCESS_TOKEN").toString()

        val timezones = loadTimezones()
        val timezonesMap = timezones.associate { it.text to it.utc.firstOrNull() }
        val timezoneText = timezones.map { it.text }
        val timezoneAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, timezoneText)
        txtTimezone.setAdapter(timezoneAdapter)

        txtTimezone.setOnItemClickListener { parent, _, position, _ ->
            val selectedTimezoneText = parent.getItemAtPosition(position) as String
            val selectedTimezoneUtc = timezonesMap[selectedTimezoneText]
            timezone = selectedTimezoneUtc.toString()
        }

        setupSpinner()

        btnClose.setOnClickListener { finish() }

        btnInfo.setOnClickListener{
            AlertDialog.Builder(this)
                .setTitle("Informação sobre as Unidades de Medida")
                .setMessage("As unidades aqui inseridas estão por padrão em Toneladas, se desejar as altere nas configurações posteriormente")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        btnEditAll.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_edit_all, null)
            val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroup)
            val txtValue = dialogView.findViewById<EditText>(R.id.txtValue)

            AlertDialog.Builder(this)
                .setTitle("Edite todas as metas")
                .setView(dialogView)
                .setPositiveButton("Confirmar") { dialog, _ ->
                    val selectedId = radioGroup.checkedRadioButtonId
                    val selectedFrequency = when (selectedId) {
                        R.id.radioDaily -> "Daily"
                        R.id.radioWeekly -> "Weekly"
                        R.id.radioMonthly -> "Monthly"
                        R.id.radioAnnual -> "Annual"
                        else -> "Not Selected"
                    }

                    val inputValueString = txtValue.text.toString()

                    if (selectedFrequency == "Not Selected") {
                        showToast("Selecione o Parâmetro")
                    } else if (inputValueString.isEmpty()) {
                        showToast("Preencha o campo de meta")
                    } else {
                        try {
                            val inputValue = inputValueString.toInt()
                            when (selectedFrequency) {
                                "Daily" -> {
                                    txtDailyGoal.setText(inputValue.toString())
                                    txtWeeklyGoal.setText((inputValue * 7).toString())
                                    txtMonthlyGoal.setText((inputValue * 30).toString())
                                    txtAnnualGoal.setText((inputValue * 365).toString())
                                }
                                "Weekly" -> {
                                    txtDailyGoal.setText((inputValue / 7).toString())
                                    txtWeeklyGoal.setText(inputValue.toString())
                                    txtMonthlyGoal.setText((inputValue * 4).toString())
                                    txtAnnualGoal.setText((inputValue * 52).toString())
                                }
                                "Monthly" -> {
                                    txtDailyGoal.setText((inputValue / 30).toString())
                                    txtWeeklyGoal.setText((inputValue / 4).toString())
                                    txtMonthlyGoal.setText(inputValue.toString())
                                    txtAnnualGoal.setText((inputValue * 12).toString())
                                }
                                "Annual" -> {
                                    txtDailyGoal.setText((inputValue / 365).toString())
                                    txtWeeklyGoal.setText((inputValue / 52).toString())
                                    txtMonthlyGoal.setText((inputValue / 12).toString())
                                    txtAnnualGoal.setText(inputValue.toString())
                                }
                            }
                            showToast("Metas definidas!")
                        } catch (e: NumberFormatException) {
                            showToast("Valor inválido. Insira um número.")
                        }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        btnCreateTeam.setOnClickListener {
            when (step) {
                0 -> {
                    step2.setBackgroundResource(R.drawable.step_active)
                    step2.setTextColor((ContextCompat.getColor(this, R.color.white)))
                    midStep1.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                    linearInfo.animate().alpha(0f).setDuration(300).withEndAction {
                        linearGoals.alpha = 1f
                        linearInfo.visibility = View.GONE
                        linearGoals.visibility = View.VISIBLE
                        btnStepBack.visibility = View.VISIBLE
                        btnCreateTeam.text = "Criar Equipe"
                    }
                    step++
                }
                1->{
                    createTeam()
                }
            }
        }

        btnStepBack.setOnClickListener {
            when (step) {
                1 -> {
                    step2.setBackgroundResource(R.drawable.step_inactive)
                    step2.setTextColor((ContextCompat.getColor(this, R.color.black)))
                    midStep1.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))

                    linearGoals.animate().alpha(0f).setDuration(300).withEndAction {
                        linearGoals.visibility = View.GONE
                        linearInfo.visibility = View.VISIBLE
                        linearInfo.alpha = 1f
                        btnStepBack.visibility = View.GONE
                        btnCreateTeam.text = "Próximo Passo"
                    }
                    step--
                }
            }
        }

    }

    private fun createTeam() {
        loadingProgressBar.visibility = View.VISIBLE
        overlayView.visibility = View.VISIBLE
        val members: List<Member> = listOf(Member(userId, "ADMINISTRATOR"))
        val teamsRequest = TeamsRequest(
            txtHandle.text.toString(),
            txtTeamName.text.toString(),
            txtDescription.text.toString(),
            ActivitiesRequest(activitySelected),
            txtDailyGoal.text.toString().toDouble(),
            txtWeeklyGoal.text.toString().toDouble(),
            txtMonthlyGoal.text.toString().toDouble(),
            txtAnnualGoal.text.toString().toDouble(),
            timezone,
            members
        )
        teamsViewModel.createTeam(accessToken, teamsRequest){
            finish()
        }
    }

    private fun loadTimezones(): List<Timezone> {
        val jsonFileString = getTimezone("timezones.json")
        val gson = Gson()
        val listTimezoneType = object : TypeToken<List<Timezone>>() {}.type
        return gson.fromJson(jsonFileString, listTimezoneType)
    }

    private fun getTimezone(fileName: String): String? {
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
