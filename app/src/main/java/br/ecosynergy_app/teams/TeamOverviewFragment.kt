package br.ecosynergy_app.teams

import android.app.AlertDialog
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.home.HomeActivity
import br.ecosynergy_app.login.LoginActivity
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.Members
import br.ecosynergy_app.room.MembersRepository
import br.ecosynergy_app.room.TeamsRepository
import br.ecosynergy_app.room.UserRepository
import br.ecosynergy_app.user.UserViewModel
import br.ecosynergy_app.user.UserViewModelFactory
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

class TeamOverviewFragment : Fragment(R.layout.fragment_team_overview) {

    private lateinit var userViewModel: UserViewModel
    private lateinit var teamsViewModel: TeamsViewModel

    private lateinit var teamPicture: CircleImageView

    private lateinit var txtTeamName: TextInputEditText
    private lateinit var txtHandle: TextInputEditText
    private lateinit var txtDescription: TextInputEditText
    private lateinit var txtTimezone: MaterialAutoCompleteTextView
    private lateinit var spinnerActivities: Spinner
    private lateinit var txtPlan: TextInputEditText

    private lateinit var btnEdit: MaterialButton

    private lateinit var btnDelete: MaterialButton

    private lateinit var shimmerImg: ShimmerFrameLayout

    private var accessToken: String = ""

    private var timezone : String = ""

    private var utcToTextMap: Map<String?, String> = mapOf()

    private var teamName: String = ""
    private var teamId: Int = 0
    private var teamDescription: String = ""
    private var teamTimezone: String = ""

    private var userId: Int = 0
    private var teamHandle: String = ""
    private var members: List<Members> = emptyList()

    private var isEditing: Boolean = false

    var userRole : String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_team_overview, container, false)

        val userDao = AppDatabase.getDatabase(requireContext()).userDao()
        val userRepository = UserRepository(userDao)

        val teamsDao = AppDatabase.getDatabase(requireContext()).teamsDao()
        val teamsRepository = TeamsRepository(teamsDao)

        val membersDao = AppDatabase.getDatabase(requireContext()).membersDao()
        val membersRepository = MembersRepository(membersDao)

        teamsViewModel = ViewModelProvider(
            this,
            TeamsViewModelFactory(RetrofitClient.teamsService, teamsRepository, membersRepository)
        )[TeamsViewModel::class.java]
        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(RetrofitClient.userService, userRepository)
        )[UserViewModel::class.java]

        teamId = requireArguments().getInt("TEAM_ID")
        teamHandle = requireArguments().getString("TEAM_HANDLE").toString()
        userId = requireArguments().getInt("USER_ID")
        accessToken = requireArguments().getString("ACCESS_TOKEN").toString()

        teamPicture = view.findViewById(R.id.teamPicture)
        txtTeamName = view.findViewById(R.id.txtTeamName)
        txtHandle = view.findViewById(R.id.txtHandle)
        txtTimezone = view.findViewById(R.id.txtTimezone)
        txtDescription = view.findViewById(R.id.txtDescription)
        spinnerActivities = view.findViewById(R.id.spinnerActivities)
        txtPlan = view.findViewById(R.id.txtPlan)

        btnEdit = view.findViewById(R.id.btnEdit)

        btnDelete = view.findViewById(R.id.btnDelete)

        shimmerImg = view.findViewById(R.id.shimmerImg)

        txtTimezone.isEnabled = false
        txtTimezone.setTextColor(ContextCompat.getColor(requireContext(), R.color.disabled))


        val timezones = loadTimezones()
        val timezonesMap = timezones.associate { it.text to it.utc.firstOrNull() }
        utcToTextMap = timezones.associate { it.utc.firstOrNull() to it.text }
        val timezoneText = timezones.map { it.text }
        val timezoneAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, timezoneText)
        txtTimezone.setAdapter(timezoneAdapter)

        txtTimezone.setOnItemClickListener { parent, _, position, _ ->
            val selectedTimezoneText = parent.getItemAtPosition(position) as String
            val selectedTimezoneUtc = timezonesMap[selectedTimezoneText]
            timezone = selectedTimezoneUtc.toString()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeTeamInfo()

        spinnerActivities.isEnabled = false

        btnDelete.text = "Excluir $teamHandle"
        btnDelete.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Você deseja excluir $teamHandle?")
            builder.setMessage("Se excluir esta equipe, perderá todos os dados armazenados nela.")

            builder.setPositiveButton("Sim") { dialog, _ ->
                deleteTeam()
                dialog.dismiss()
                requireActivity().supportFragmentManager.popBackStack()
            }

            builder.setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        btnEdit.setOnClickListener {
            if (!isEditing) {
                isEditing = true
                enableEditTexts()
                btnEdit.icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.baseline_check_24)
                btnEdit.text = "Confirmar edição"
            } else {
                teamDescription = txtDescription.text.toString()
                teamTimezone = timezone
                teamName = txtTeamName.text.toString()

                isEditing = false
                editTeamInfo()
                disableEditTexts()
                LoginActivity().showSnackBar("Informações editadas com sucesso!", "FECHAR", R.color.greenDark)
                btnEdit.icon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_edit_24)
                btnEdit.text = "Editar dados"
            }
        }
    }

    private fun editTeamInfo() {
        teamsViewModel.updateTeam(
            accessToken,
            teamId,
            UpdateRequest(teamHandle, teamName, teamDescription, teamTimezone)
        )
    }

    private fun getThemeColor(attrResId: Int): Int {
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(attrResId, typedValue, true)
        return typedValue.data
    }

    private fun enableEditTexts() {
        txtTimezone.isEnabled = true
        txtHandle.isEnabled = true
        txtDescription.isEnabled = true
        txtTeamName.isEnabled = true
        spinnerActivities.isEnabled = true
        txtPlan.isEnabled = true

        txtTimezone.setTextColor(getThemeColor(android.R.attr.textColorPrimary))

    }

    private fun disableEditTexts() {
        txtTimezone.isEnabled = false
        txtHandle.isEnabled = false
        txtDescription.isEnabled = false
        txtTeamName.isEnabled = false
        spinnerActivities.isEnabled = false
        txtPlan.isEnabled = false

        txtTimezone.setTextColor(ContextCompat.getColor(requireContext(), R.color.disabled))
    }

    private fun deleteTeam() {
        teamsViewModel.deleteTeam(accessToken, teamId)
    }

    private fun observeTeamInfo() {
        teamsViewModel.getTeamById(teamId)
        teamsViewModel.teamDB.observe(viewLifecycleOwner) { teamInfo ->
            teamsViewModel.getMembersByTeamId(teamId)
            teamsViewModel.allMembersDB.observe(viewLifecycleOwner) { membersInfo ->

                teamId = teamInfo.id
                teamName = teamInfo.name
                members = membersInfo

                val userMember = members.find { it.userId == userId }
                userRole = userMember?.role.toString()

                if (userRole == "ADMINISTRATOR") {
                    btnEdit.visibility = View.VISIBLE
                    btnDelete.visibility = View.VISIBLE
                } else {
                    btnEdit.visibility = View.GONE
                    btnDelete.visibility = View.GONE
                }

                shimmerImg.visibility = View.VISIBLE
                teamPicture.visibility = View.GONE

                val drawableId = HomeActivity().getDrawableForLetter(teamName.first())
                teamPicture.setImageResource(drawableId)
                txtTeamName.setText(teamName)
                txtHandle.setText(teamInfo.handle)
                txtDescription.setText(teamInfo.description)

                txtTimezone.setText(utcToTextMap[teamInfo.timeZone])

                shimmerImg.animate().alpha(0f).setDuration(300).withEndAction {
                    shimmerImg.stopShimmer()
                    shimmerImg.animate().alpha(1f).setDuration(300)
                    shimmerImg.visibility = View.GONE
                    teamPicture.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun loadTimezones(): List<Timezone> {
        val jsonString = getTimezone("timezones.json")
        val gson = Gson()
        val listType = object : TypeToken<List<Timezone>>() {}.type
        return gson.fromJson(jsonString, listType)
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