package br.ecosynergy_app.teams

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.register.Nationality
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.TeamsRepository
import br.ecosynergy_app.user.UserViewModel
import br.ecosynergy_app.user.UserViewModelFactory
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
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

    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var overlayView: View

    private var token: String? = ""

    private var teamHandle: String? = ""
    private var teamName: String? = ""
    private var teamId: Int = 0
    private var teamDescription: String? = ""
    private var teamTimezone: String? = ""

    private var userId: String? = ""
    private var members: List<Member> = emptyList()

    private var isEditing: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_team_overview, container, false)

        val teamsDao = AppDatabase.getDatabase(requireContext()).teamsDao()
        val teamsRepository = TeamsRepository(teamsDao)

        teamsViewModel = ViewModelProvider(this, TeamsViewModelFactory(RetrofitClient.teamsService, teamsRepository))[TeamsViewModel::class.java]
        userViewModel = ViewModelProvider(this, UserViewModelFactory(RetrofitClient.userService))[UserViewModel::class.java]

        val sp: SharedPreferences = requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        token = sp.getString("accessToken", null)
        userId = sp.getString("id", null)

        teamHandle = arguments?.getString("TEAM_HANDLE")

        val timezones = loadTimezones()
        val timezoneNames = timezones.map { it.text }

        Log.d("TeamOverviewFragment", "Team Handle: $teamHandle")

        teamPicture = view.findViewById(R.id.teamPicture)
        txtTeamName = view.findViewById(R.id.txtTeamName)
        txtHandle = view.findViewById(R.id.txtHandle)
        txtTimezone = view.findViewById(R.id.txtTimezone)
        txtDescription = view.findViewById(R.id.txtDescription)
        spinnerActivities = view.findViewById(R.id.spinnerActivities)
        txtPlan = view.findViewById(R.id.txtPlan)

        btnEdit = view.findViewById(R.id.btnEdit)

        btnDelete = view.findViewById(R.id.btnDelete)

        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)
        overlayView = view.findViewById(R.id.overlayView)

        shimmerImg = view.findViewById(R.id.shimmerImg)

        txtTimezone.isEnabled = false
        txtTimezone.setTextColor(ContextCompat.getColor(requireContext(), R.color.disabled))

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, timezoneNames)
        txtTimezone.setAdapter(adapter)



        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeTeamInfo()

        spinnerActivities.isEnabled = false

        btnDelete.text = "Excluir $teamHandle"
        btnDelete.setOnClickListener{
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

        btnEdit.setOnClickListener{
            if(!isEditing){
                isEditing = true
                enableEditTexts()
                btnEdit.icon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_check_24)
                btnEdit.text = "Confirmar edição"
            }else{
                isEditing = false
                editTeamInfo()
                disableEditTexts()
                showSnackBar("Informações editadas com sucesso!","FECHAR", R.color.greenDark)
                btnEdit.icon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_edit_24)
                btnEdit.text = "Editar dados"
            }
        }
    }

    private fun editTeamInfo(){
        teamsViewModel.updateTeam(token, teamId, UpdateRequest(teamHandle, teamName, teamDescription, teamTimezone))
    }

    private fun getThemeColor(attrResId: Int): Int {
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(attrResId, typedValue, true)
        return typedValue.data
    }

    private fun enableEditTexts(){
        txtTimezone.isEnabled = true
        txtHandle.isEnabled = true
        txtDescription.isEnabled = true
        txtTeamName.isEnabled = true
        spinnerActivities.isEnabled = true
        txtPlan.isEnabled = true

        txtTimezone.setTextColor(getThemeColor(android.R.attr.textColorPrimary))

    }

    private fun disableEditTexts(){
        txtTimezone.isEnabled = false
        txtHandle.isEnabled = false
        txtDescription.isEnabled = false
        txtTeamName.isEnabled = false
        spinnerActivities.isEnabled = false
        txtPlan.isEnabled = false

        txtTimezone.setTextColor(ContextCompat.getColor(requireContext(), R.color.disabled))
    }


    private fun deleteTeam(){
        teamsViewModel.deleteTeam(token, teamId)
    }

    private fun observeTeamInfo(){
        teamsViewModel.findTeamByHandle(token, teamHandle)
        teamsViewModel.teamResult.observe(viewLifecycleOwner){ result ->
            result.onSuccess { response ->
                teamId = response.id
                teamName = response.name
                members = response.members

                val userMember = members.find { it.id.toString() == userId }
                val userRole = userMember?.role

                if (userRole == "ADMINISTRATOR") {
                    btnEdit.visibility = View.VISIBLE
                    btnDelete.visibility = View.VISIBLE
                } else {
                    btnEdit.visibility = View.GONE
                    btnDelete.visibility = View.GONE
                }

                shimmerImg.visibility = View.VISIBLE
                teamPicture.visibility = View.GONE

                val drawableId = getDrawableForLetter(teamName!!.first())
                teamPicture.setImageResource(drawableId)
                txtTeamName.setText(teamName)
                txtHandle.setText(response.handle)
                txtDescription.setText(response.description)
                txtTimezone.setText(response.timeZone)

                shimmerImg.animate().alpha(0f).setDuration(300).withEndAction {
                    shimmerImg.stopShimmer()
                    shimmerImg.animate().alpha(1f).setDuration(300)
                    shimmerImg.visibility = View.GONE
                    teamPicture.visibility = View.VISIBLE
                }
            }.onFailure { error ->
                error.printStackTrace()
                Log.d("TeamOverviewFragment", "Team Result Failed: ${error.message}")
                shimmerImg.visibility = View.VISIBLE
                teamPicture.visibility = View.GONE
            }
        }
    }

    private fun getDrawableForLetter(letter: Char): Int {
        return when (letter.lowercaseChar()) {
            'a' -> R.drawable.a
            'b' -> R.drawable.b
            'c' -> R.drawable.c
            'd' -> R.drawable.d
            'e' -> R.drawable.e
            'f' -> R.drawable.f
            'g' -> R.drawable.g
            'h' -> R.drawable.h
            'i' -> R.drawable.i
            'j' -> R.drawable.j
            'k' -> R.drawable.k
            'l' -> R.drawable.l
            'm' -> R.drawable.m
            'n' -> R.drawable.n
            'o' -> R.drawable.o
            'p' -> R.drawable.p
            'q' -> R.drawable.q
            'r' -> R.drawable.r
            's' -> R.drawable.s
            't' -> R.drawable.t
            'u' -> R.drawable.u
            'v' -> R.drawable.v
            'w' -> R.drawable.w
            'x' -> R.drawable.x
            'y' -> R.drawable.y
            'z' -> R.drawable.z
            else -> R.drawable.default_image
        }
    }

    private fun showSnackBar(message: String, action: String, bgTint: Int) {
        val rootView = requireView()
        val snackBar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT)
            .setAction(action) {}
        snackBar.setBackgroundTint(ContextCompat.getColor(requireContext(), bgTint))
        snackBar.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        snackBar.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        snackBar.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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