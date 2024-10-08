package br.ecosynergy_app.teams

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Spinner
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.teams.MembersRepository
import br.ecosynergy_app.room.teams.TeamsRepository
import br.ecosynergy_app.teams.sectors.ActivitiesRequest
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class CreateTeamBottomSheet : BottomSheetDialogFragment() {

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

    private lateinit var loadingProgressBar: ProgressBar

    private var accessToken: String = ""
    private var userId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_team_bottom_sheet, container, false)

        val teamsDao = AppDatabase.getDatabase(requireContext()).teamsDao()
        val teamsRepository = TeamsRepository(teamsDao)

        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)

        btnClose = view.findViewById(R.id.btnClose)

        val membersDao = AppDatabase.getDatabase(requireContext()).membersDao()
        val membersRepository = MembersRepository(membersDao)

        teamsViewModel = ViewModelProvider(requireActivity(), TeamsViewModelFactory(RetrofitClient.teamsService, teamsRepository, membersRepository))[TeamsViewModel::class.java]

        txtHandle = view.findViewById(R.id.txtHandle)
        txtTeamName = view.findViewById(R.id.txtTeamName)
        txtDescription = view.findViewById(R.id.txtDescription)
        spinnerActivities = view.findViewById(R.id.spinnerActivities)
        btnCreateTeam = view.findViewById(R.id.btnCreateTeam)
        txtTimezone = view.findViewById(R.id.txtTimezone)
        txtDailyGoal = view.findViewById(R.id.txtDailyGoal)
        txtWeeklyGoal = view.findViewById(R.id.txtWeeklyGoal)
        txtMonthlyGoal = view.findViewById(R.id.txtMonthlyGoal)
        txtAnnualGoal = view.findViewById(R.id.txtAnnualGoal)

        userId = requireArguments().getInt("USER_ID")
        accessToken = requireArguments().getString("ACCESS_TOKEN").toString()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.peekHeight = 2000
        }

        val timezones = loadTimezones()
        val timezonesMap = timezones.associate { it.text to it.utc.firstOrNull() }
        val timezoneText = timezones.map { it.text }
        val timezoneAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, timezoneText)
        txtTimezone.setAdapter(timezoneAdapter)

        txtTimezone.setOnItemClickListener { parent, _, position, _ ->
            val selectedTimezoneText = parent.getItemAtPosition(position) as String
            val selectedTimezoneUtc = timezonesMap[selectedTimezoneText]
            timezone = selectedTimezoneUtc.toString()
        }

        spinnerActivities.isEnabled = false

        btnClose.setOnClickListener{ dismiss() }

        btnCreateTeam.setOnClickListener{ createTeam() }

    }

    private fun createTeam() {
        loadingProgressBar.visibility = View.VISIBLE
        val members: List<Member> = listOf(Member(userId, "ADMINISTRATOR"))
        val teamsRequest = TeamsRequest(
            txtHandle.text.toString(),
            txtTeamName.text.toString(),
            txtDescription.text.toString(),
            ActivitiesRequest(1),
            txtDailyGoal.text.toString().toDouble(),
            txtWeeklyGoal.text.toString().toDouble(),
            txtMonthlyGoal.text.toString().toDouble(),
            txtAnnualGoal.text.toString().toDouble(),
            timezone,
            members
        )
        teamsViewModel.createTeam(accessToken, teamsRequest)
        dismiss()
    }

    private fun loadTimezones(): List<Timezone> {
        val jsonFileString = getTimezone("timezones.json")
        val gson = Gson()
        val listTimezoneType = object : TypeToken<List<Timezone>>() {}.type
        return gson.fromJson(jsonFileString, listTimezoneType)
    }

    private fun getTimezone(fileName: String): String? {
        return try {
            val inputStream = requireContext().assets.open(fileName)
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
}

