package br.ecosynergy_app.teams

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.invites.InvitesRepository
import br.ecosynergy_app.room.teams.Members
import br.ecosynergy_app.room.teams.MembersRepository
import br.ecosynergy_app.room.teams.TeamsRepository
import br.ecosynergy_app.room.user.UserRepository
import br.ecosynergy_app.teams.viewmodel.TeamsViewModel
import br.ecosynergy_app.teams.viewmodel.TeamsViewModelFactory
import br.ecosynergy_app.user.viewmodel.UserViewModel
import br.ecosynergy_app.user.viewmodel.UserViewModelFactory
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.textfield.TextInputEditText

class TeamMembersActivity : AppCompatActivity() {

    private lateinit var recycleMembers: RecyclerView
    lateinit var membersAdapter: MembersAdapter
    private lateinit var shimmerMembers: ShimmerFrameLayout

    private lateinit var userViewModel: UserViewModel
    private lateinit var teamsViewModel: TeamsViewModel

    private lateinit var swipeRefresh: SwipeRefreshLayout

    private lateinit var txtSearch: TextInputEditText
    private lateinit var btnAddMember: ImageButton

    private lateinit var btnBack : ImageButton

    private lateinit var userRepository: UserRepository
    private lateinit var teamsRepository: TeamsRepository
    private lateinit var membersRepository: MembersRepository
    private lateinit var invitesRepository: InvitesRepository

    private var membersList: MutableList<Members> = mutableListOf()
    private var memberIds: MutableList<Int> = mutableListOf()
    private var memberRoles: MutableList<String> = mutableListOf()
    private var currentUserRole: String? = ""

    private var userId: Int = 0
    private var teamId: Int = 0
    private var accessToken: String = ""
    private var teamHandle: String = ""
    private var userRole: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_members)

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

        teamId = intent.getIntExtra("TEAM_ID", 0)
        teamHandle = intent.getStringExtra("TEAM_HANDLE").toString()
        userId = intent.getIntExtra("USER_ID", 0)
        accessToken = intent.getStringExtra("ACCESS_TOKEN").toString()
        userRole = intent.getStringExtra("USER_ROLE").toString()

        shimmerMembers = findViewById(R.id.shimmerMembers)
        recycleMembers = findViewById(R.id.recycleMembers)
        txtSearch = findViewById(R.id.txtSearch)
        btnAddMember = findViewById(R.id.btnAddMember)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        btnBack = findViewById(R.id.btnBack)

        btnAddMember.visibility = if (userRole == "ADMINISTRATOR"|| userRole == "FOUNDER") View.VISIBLE else View.GONE

        observeMembersInfo()
        setupSwipeRefresh()


        btnBack.setOnClickListener{ finish() }

        txtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterMembers(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        btnAddMember.setOnClickListener {
            val memberIdsString = memberIds.joinToString(",")
            val i = Intent(this, AddMembersActivity::class.java).apply {
                putExtra("TEAM_HANDLE", teamHandle)
                putExtra("TEAM_ID", teamId)
                putExtra("MEMBER_IDS", memberIdsString)
                putExtra("USER_ID", userId)
                putExtra("ACCESS_TOKEN", accessToken)
            }
            startActivity(i)
        }

    }

    private fun observeMembersInfo() {
        shimmerMembers.visibility = View.VISIBLE
        recycleMembers.visibility = View.GONE

        teamsViewModel.getMembersByTeamId(teamId)
        teamsViewModel.allMembersDB.observe(this) { membersResponse ->

            // Update lists with the new data
            memberIds = membersResponse.map { it.userId }.toMutableList()
            memberRoles = membersResponse.map { it.role }.toMutableList()
            membersList = membersResponse.toMutableList()

            val userMember = membersResponse.find { it.userId == userId }
            currentUserRole = userMember?.role


            if (membersList.isNotEmpty()) {
                initializeAdapter()
            }

            membersAdapter.updateList(membersList, memberRoles)

            shimmerMembers.animate().alpha(0f).setDuration(300).withEndAction {
                shimmerMembers.stopShimmer()
                shimmerMembers.visibility = View.GONE
                recycleMembers.visibility = View.VISIBLE
            }
        }
    }

    private fun initializeAdapter() {
        recycleMembers.layoutManager = LinearLayoutManager(this)

        membersAdapter = MembersAdapter(
            membersList,
            memberRoles,
            currentUserRole,
            teamId,
            userId,
            accessToken,
            teamsViewModel,
            this,
            memberIds
        )

        recycleMembers.adapter = membersAdapter
    }


    private fun filterMembers(query: String) {
        val filteredList = membersList.filter {
            it.fullName.contains(query, ignoreCase = true) || it.username.contains(
                query,
                ignoreCase = true
            )
        }
        membersAdapter.updateList(filteredList, memberRoles)
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            observeMembersInfo()
            swipeRefresh.isRefreshing = false
        }
    }
}
