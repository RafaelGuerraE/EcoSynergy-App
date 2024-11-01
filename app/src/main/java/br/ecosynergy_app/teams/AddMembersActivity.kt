package br.ecosynergy_app.teams

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class AddMembersActivity : AppCompatActivity() {

    private lateinit var btnClose: ImageButton
    private lateinit var txtMember: TextInputEditText
    private lateinit var btnSearch: ImageButton

    private lateinit var teamsViewModel: TeamsViewModel
    private lateinit var userViewModel: UserViewModel

    private lateinit var recycleUsers: RecyclerView
    lateinit var usersAdapter: UsersAdapter
    private lateinit var shimmerUsers: ShimmerFrameLayout

    private lateinit var swipeRefresh: SwipeRefreshLayout

    private lateinit var userRepository: UserRepository
    private lateinit var teamsRepository: TeamsRepository
    private lateinit var membersRepository: MembersRepository
    private lateinit var invitesRepository: InvitesRepository

    private var accessToken: String = ""
    private var teamId: Int = 0
    private var teamHandle: String = ""
    private var memberIds: ArrayList<String> = arrayListOf()

    private var userId: Int = 0
    private var username: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_members)

        val db = AppDatabase.getDatabase(applicationContext)
        userRepository = UserRepository(db.userDao())
        teamsRepository = TeamsRepository(db.teamsDao())
        membersRepository = MembersRepository(db.membersDao())
        invitesRepository = InvitesRepository(db.invitesDao())

        teamsViewModel = ViewModelProvider(
            this,
            TeamsViewModelFactory(RetrofitClient.teamsService, teamsRepository, RetrofitClient.invitesService, membersRepository, invitesRepository)
        )[TeamsViewModel::class.java]

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(RetrofitClient.userService, userRepository)
        )[UserViewModel::class.java]

        btnClose = findViewById(R.id.btnClose)
        txtMember = findViewById(R.id.txtMember)
        btnSearch = findViewById(R.id.btnSearch)
        shimmerUsers = findViewById(R.id.shimmerUsers)
        recycleUsers = findViewById(R.id.recycleUsers)

        shimmerUsers.visibility = View.GONE
        recycleUsers.visibility = View.GONE

        intent?.let {
            teamHandle = it.getStringExtra("TEAM_HANDLE") ?: ""
            teamId = it.getIntExtra("TEAM_ID", 0)
            val memberIdsString = it.getStringExtra("MEMBER_IDS") ?: ""
            memberIds = ArrayList(memberIdsString.split(","))
            accessToken = it.getStringExtra("ACCESS_TOKEN") ?: ""
        }

        setupRecyclerView()

        btnClose.setOnClickListener {
            finish()
        }

        btnSearch.setOnClickListener {
            searchUser(txtMember.text.toString(), accessToken)
        }
    }


    private fun setupRecyclerView() {
        recycleUsers.layoutManager = LinearLayoutManager(this)
        usersAdapter = UsersAdapter(
            mutableListOf(),
            teamId,
            teamHandle,
            teamsViewModel,
            memberIds,
            accessToken,
            userId,
            username,
            this
        )
        recycleUsers.adapter = usersAdapter

        Log.d("AddMembersActivity", "MemberIDs in setupRecyclerView: $memberIds, $teamHandle, $teamId")
    }


    private fun searchUser(username: String, accessToken: String) {
        userViewModel.searchUser(username, accessToken)
        userViewModel.users.observe(this) { result ->
            result.onSuccess { usersList ->
                shimmerUsers.visibility = View.VISIBLE
                recycleUsers.visibility = View.GONE

                usersAdapter = UsersAdapter(
                    usersList,
                    teamId,
                    teamHandle,
                    teamsViewModel,
                    memberIds,
                    accessToken,
                    userId,
                    username,
                    this
                )
                recycleUsers.adapter = usersAdapter

                shimmerUsers.animate().alpha(0f).setDuration(300).withEndAction {
                    shimmerUsers.stopShimmer()
                    shimmerUsers.animate().alpha(1f).setDuration(300)
                    shimmerUsers.visibility = View.GONE
                    recycleUsers.visibility = View.VISIBLE
                }
            }.onFailure { error ->
                error.printStackTrace()
                Log.d("AddMembersActivity", "User Result Failed: ${error.message}")
                shimmerUsers.visibility = View.GONE
                recycleUsers.visibility = View.GONE
                showSnackBar("ERRO: Carregar usuários", "FECHAR", R.color.red)
            }
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
}
