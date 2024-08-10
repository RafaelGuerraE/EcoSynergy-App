package br.ecosynergy_app.teams

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.login.AuthViewModel
import br.ecosynergy_app.login.AuthViewModelFactory
import br.ecosynergy_app.user.MembersAdapter
import br.ecosynergy_app.user.UserViewModel
import br.ecosynergy_app.user.UserViewModelFactory
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import de.hdodenhof.circleimageview.CircleImageView

class TeamOverviewFragment : Fragment(R.layout.fragment_team_overview) {

    private lateinit var userViewModel: UserViewModel
    private lateinit var teamsViewModel: TeamsViewModel

    private lateinit var teamPicture: CircleImageView

    private lateinit var txtTeamName: TextInputEditText
    private lateinit var txtHandle: TextInputEditText
    private lateinit var txtDescription: TextInputEditText
    private lateinit var txtTimezone: MaterialAutoCompleteTextView
    private lateinit var txtSector: TextInputEditText
    private lateinit var txtPlan: TextInputEditText

    private lateinit var btnEditTeamName: ImageButton
    private lateinit var btnEditHandle: ImageButton
    private lateinit var btnEditDescription: ImageButton
    private lateinit var btnEditTimezone: ImageButton
    private lateinit var btnEditSector: ImageButton
    private lateinit var btnEditPlan: ImageButton

    private lateinit var btnDelete: MaterialButton

    private lateinit var shimmerImg: ShimmerFrameLayout

    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var overlayView: View

    private var token: String? = ""
    private var teamHandle: String? = ""
    private var teamName: String? = ""
    private var teamId: String? = ""

    private var userId: String? = ""
    private var members: List<Member> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_team_overview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        teamsViewModel = ViewModelProvider(this, TeamsViewModelFactory(RetrofitClient.teamsService))[TeamsViewModel::class.java]
        userViewModel = ViewModelProvider(this, UserViewModelFactory(RetrofitClient.userService))[UserViewModel::class.java]

        val sp: SharedPreferences = requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        token = sp.getString("accessToken", null)
        userId = sp.getString("id", null)

        teamHandle = arguments?.getString("TEAM_HANDLE")

        Log.d("TeamOverviewFragment", "Team Handle: $teamHandle")

        teamPicture = view.findViewById(R.id.teamPicture)
        txtTeamName = view.findViewById(R.id.txtTeamName)
        txtHandle = view.findViewById(R.id.txtHandle)
        txtTimezone = view.findViewById(R.id.txtTimezone)
        txtDescription = view.findViewById(R.id.txtDescription)
        txtSector = view.findViewById(R.id.txtSector)
        txtPlan = view.findViewById(R.id.txtPlan)

        btnEditTeamName = view.findViewById(R.id.btnEditTeamName)
        btnEditHandle = view.findViewById(R.id.btnEditHandle)
        btnEditDescription = view.findViewById(R.id.btnEditDescription)
        btnEditTimezone = view.findViewById(R.id.btnEditTimezone)
        btnEditSector = view.findViewById(R.id.btnEditSector)
        btnEditPlan = view.findViewById(R.id.btnEditPlan)

        btnDelete = view.findViewById(R.id.btnDelete)

        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)
        overlayView = view.findViewById(R.id.overlayView)

        shimmerImg = view.findViewById(R.id.shimmerImg)

        txtTimezone.isEnabled = false
        txtTimezone.setTextColor(ContextCompat.getColor(requireContext(), R.color.disabled))

        observeTeamInfo()

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
                    allowEdit()
                } else {
                    hideEditButtons()
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

    private fun allowEdit() {
        // Make all edit buttons visible
        btnEditTeamName.visibility = View.VISIBLE
        btnEditHandle.visibility = View.VISIBLE
        btnEditDescription.visibility = View.VISIBLE
        btnEditTimezone.visibility = View.VISIBLE
        btnEditSector.visibility = View.VISIBLE
        btnEditPlan.visibility = View.VISIBLE
    }

    private fun hideEditButtons() {
        btnEditTeamName.visibility = View.GONE
        btnEditHandle.visibility = View.GONE
        btnEditDescription.visibility = View.GONE
        btnEditTimezone.visibility = View.GONE
        btnEditSector.visibility = View.GONE
        btnEditPlan.visibility = View.GONE
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

    private fun setEditButtons(button: ImageButton) {
        when (button) {
            btnEditTeamName -> {
                enableOnly(btnEditTeamName)
            }
            btnEditHandle -> {
                enableOnly(btnEditHandle)
            }
            btnEditDescription -> {
                enableOnly(btnEditDescription)
            }
            btnEditTimezone -> {
                enableOnly(btnEditTimezone)
            }
            btnEditSector -> {
                enableOnly(btnEditSector)
            }
            btnEditPlan -> {
                enableOnly(btnEditPlan)
            }
        }
    }

    private fun enableOnly(enabledButton: ImageButton) {
        btnEditTeamName.isEnabled = enabledButton == btnEditTeamName
        btnEditHandle.isEnabled = enabledButton == btnEditHandle
        btnEditDescription.isEnabled = enabledButton == btnEditDescription
        btnEditTimezone.isEnabled = enabledButton == btnEditTimezone
        btnEditSector.isEnabled = enabledButton == btnEditSector
        btnEditPlan.isEnabled = enabledButton == btnEditPlan
    }

    private fun enableAllButtons() {
        btnEditTeamName.isEnabled = true
        btnEditHandle.isEnabled = true
        btnEditDescription.isEnabled = true
        btnEditTimezone.isEnabled = true
        btnEditSector.isEnabled = true
        btnEditPlan.isEnabled = true
    }
}