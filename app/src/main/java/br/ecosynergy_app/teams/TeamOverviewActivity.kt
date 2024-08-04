package br.ecosynergy_app.teams

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.user.MembersAdapter
import br.ecosynergy_app.user.UserViewModel
import br.ecosynergy_app.user.UserViewModelFactory
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import de.hdodenhof.circleimageview.CircleImageView

class TeamOverviewActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton

    private lateinit var recycleMembers: RecyclerView
    private lateinit var membersAdapter: MembersAdapter
    private lateinit var userViewModel: UserViewModel
    private lateinit var teamsViewModel: TeamsViewModel

    private lateinit var teamPicture: CircleImageView

    private lateinit var txtTeamName: TextInputEditText
    private lateinit var txtHandle: TextInputEditText
    private lateinit var txtDescription: TextInputEditText
    private lateinit var txtSector: TextInputEditText
    private lateinit var txtPlan: TextInputEditText

    private lateinit var btnEditTeamName: ImageButton
    private lateinit var btnEditHandle: ImageButton
    private lateinit var btnEditDescription: ImageButton
    private lateinit var btnEditSector: ImageButton
    private lateinit var btnEditPlan: ImageButton

    private lateinit var btnDelete: MaterialButton

    private lateinit var shimmerImg: ShimmerFrameLayout
    private lateinit var shimmerMembers: ShimmerFrameLayout

    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var overlayView: View

    private var token: String? = ""
    private var teamHandle: String? = ""
    private var teamId: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teamoverview)

        userViewModel = ViewModelProvider(this, UserViewModelFactory(RetrofitClient.userService))[UserViewModel::class.java]
        teamsViewModel = ViewModelProvider(this, TeamsViewModelFactory(RetrofitClient.teamsService))[TeamsViewModel::class.java]

        val sp: SharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        token = sp.getString("accessToken", null)

        teamHandle = intent.getStringExtra("TEAM_HANDLE")

        btnBack = findViewById(R.id.btnBack)

        teamPicture = findViewById(R.id.teamPicture)
        txtTeamName = findViewById(R.id.txtTeamName)
        txtHandle = findViewById(R.id.txtHandle)
        txtDescription = findViewById(R.id.txtDescription)
        txtSector = findViewById(R.id.txtSector)
        txtPlan = findViewById(R.id.txtPlan)

        btnEditTeamName = findViewById(R.id.btnEditTeamName)
        btnEditHandle = findViewById(R.id.btnEditHandle)
        btnEditDescription = findViewById(R.id.btnEditDescription)
        btnEditSector = findViewById(R.id.btnEditSector)
        btnEditPlan = findViewById(R.id.btnEditPlan)

        btnDelete = findViewById(R.id.btnDelete)

        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        overlayView = findViewById(R.id.overlayView)

        shimmerImg = findViewById(R.id.shimmerImg)
        shimmerMembers = findViewById(R.id.shimmerMembers)

        recycleMembers = findViewById(R.id.recycleMembers)

        recycleMembers.layoutManager = LinearLayoutManager(this)
        membersAdapter = MembersAdapter(emptyList(), emptyList())
        recycleMembers.adapter = membersAdapter

        loadingProgressBar.visibility = View.VISIBLE
        overlayView.visibility = View.VISIBLE

        btnBack.setOnClickListener {
            finish()
        }

        observeTeamInfo()

        btnDelete.text = "Excluir $teamHandle"
        btnDelete.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Você deseja excluir $teamHandle?")
            builder.setMessage("Se excluir esta equipe, perderá todos os dados armazenados nela.")

            builder.setPositiveButton("Sim") { dialog, _ ->
                deleteTeam()
                dialog.dismiss()
                finish()
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

    private fun observeMembersInfo(members: List<Member>){
        val memberIds = members.map { it.id.toString() }
        val memberRoles = members.map {it.role}

        userViewModel.getUsersByIds(memberIds, token)
        userViewModel.users.observe(this) { result ->
            result.onSuccess { users ->
                shimmerMembers.visibility = View.VISIBLE
                recycleMembers.visibility = View.GONE

                membersAdapter = MembersAdapter(users, memberRoles)
                recycleMembers.adapter = membersAdapter

                shimmerMembers.animate().alpha(0f).setDuration(300).withEndAction {
                    shimmerMembers.stopShimmer()
                    shimmerMembers.animate().alpha(1f).setDuration(300)
                    shimmerMembers.visibility = View.GONE
                    recycleMembers.visibility = View.VISIBLE
                }
                loadingProgressBar.visibility = View.GONE
                overlayView.visibility = View.GONE
            }.onFailure { error ->
                error.printStackTrace()
                Log.d("TeamOverviewActivity", "User Result Failed: ${error.message}")
                shimmerMembers.visibility = View.VISIBLE
                recycleMembers.visibility = View.GONE
            }
        }
    }

    private fun observeTeamInfo(){
        teamsViewModel.findTeamByHandle(token, teamHandle)
        teamsViewModel.teamResult.observe(this){ result->
            result.onSuccess { response ->
                teamId = response.id
                val teamName: String = response.name

                shimmerImg.visibility = View.VISIBLE
                teamPicture.visibility = View.GONE

                val drawableId = getDrawableForLetter(teamName.first())
                teamPicture.setImageResource(drawableId)
                txtTeamName.setText(teamName)
                txtHandle.setText(response.handle)
                txtDescription.setText(response.description)

                shimmerImg.animate().alpha(0f).setDuration(300).withEndAction {
                    shimmerImg.stopShimmer()
                    shimmerImg.animate().alpha(1f).setDuration(300)
                    shimmerImg.visibility = View.GONE
                    teamPicture.visibility = View.VISIBLE
                }

                observeMembersInfo(response.members)
            }.onFailure { error ->
                error.printStackTrace()
                Log.d("TeamOverviewActivity", "Team Result Failed: ${error.message}")
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
        val rootView = findViewById<View>(android.R.id.content)
        val snackBar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT)
            .setAction(action) {}
        snackBar.setBackgroundTint(ContextCompat.getColor(this, bgTint))
        snackBar.setTextColor(ContextCompat.getColor(this, R.color.white))
        snackBar.setActionTextColor(ContextCompat.getColor(this, R.color.white))
        snackBar.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}