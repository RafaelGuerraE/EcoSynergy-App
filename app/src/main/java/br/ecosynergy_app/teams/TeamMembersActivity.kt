package br.ecosynergy_app.teams

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
import br.ecosynergy_app.room.teams.Members
import br.ecosynergy_app.room.teams.MembersRepository
import br.ecosynergy_app.room.teams.TeamsRepository
import br.ecosynergy_app.room.user.UserRepository
import br.ecosynergy_app.teams.viewmodel.TeamsViewModel
import br.ecosynergy_app.teams.viewmodel.TeamsViewModelFactory
import br.ecosynergy_app.user.MembersAdapter
import br.ecosynergy_app.user.UserViewModel
import br.ecosynergy_app.user.UserViewModelFactory
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

    private var membersList: List<Members> = listOf()
    private var memberIds: MutableList<Int> = mutableListOf()
    private var memberRoles: List<String> = listOf()
    private var currentUserRole: String? = ""

    private var userId: Int = 0
    private var teamId: Int = 0
    private var accessToken: String = ""
    private var teamHandle: String = ""
    private var userRole: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_members)

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

        teamId = intent.getIntExtra("TEAM_ID", 0)
        teamHandle = intent.getStringExtra("TEAM_HANDLE").toString()
        userId = intent.getIntExtra("USER_ID", 0)
        accessToken = intent.getStringExtra("ACCESS_TOKEN").toString()

        shimmerMembers = findViewById(R.id.shimmerMembers)
        recycleMembers = findViewById(R.id.recycleMembers)
        txtSearch = findViewById(R.id.txtSearch)
        btnAddMember = findViewById(R.id.btnAddMember)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        btnBack = findViewById(R.id.btnBack)

        recycleMembers.layoutManager = LinearLayoutManager(this)
        membersAdapter = MembersAdapter(
            emptyList(),
            emptyList(),
            currentUserRole,
            teamId,
            userId,
            accessToken,
            teamsViewModel,
            this,
            this,
            memberIds
        )
        recycleMembers.adapter = membersAdapter

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
            val addMembersBottomSheet = AddMembersBottomSheet().apply {
                arguments = Bundle().apply {
                    putString("TEAM_HANDLE", teamHandle)
                    putInt("TEAM_ID", teamId)
                    putString("MEMBER_IDS", memberIdsString)
                    putInt("USER_ID", userId)
                    putString("ACCESS_TOKEN", accessToken)
                }
            }
            addMembersBottomSheet.show(supportFragmentManager, "AddMembersBottomSheet")
            Log.d("btnAddMember", "MemberIDS: $memberIdsString, $teamId")
        }
    }

    private fun observeMembersInfo() {
        teamsViewModel.allMembersDB.observe(this) { membersInfo ->
            val userMember = membersInfo.find { it.userId == userId }
            userRole = userMember?.role.toString()
            if (userRole == "ADMINISTRATOR") {
                btnAddMember.visibility = View.VISIBLE
            } else {
                btnAddMember.visibility = View.GONE
            }
        }

        teamsViewModel.getMembersByTeamId(teamId)
        teamsViewModel.allMembersDB.observe(this) { membersResponse ->
            memberIds = membersResponse.map { it.userId }.toMutableList()
            memberRoles = membersResponse.map { it.role }
            membersList = membersResponse

            val userMember = membersResponse.find { it.userId == userId }
            currentUserRole = userMember?.role

            shimmerMembers.visibility = View.VISIBLE
            recycleMembers.visibility = View.GONE

            val pairedMembers = membersResponse.map { member ->
                val role = membersResponse.find { it.userId.toString() == member.userId.toString() }?.role
                    ?: "Unknown"
                Pair(member, role)
            }.sortedBy { it.first.fullName }

            val sortedUsers = pairedMembers.map { it.first }
            val sortedRoles = pairedMembers.map { it.second }

            membersAdapter = MembersAdapter(
                sortedUsers,
                sortedRoles,
                currentUserRole,
                teamId,
                userId,
                accessToken,
                teamsViewModel,
                this,
                this,
                memberIds
            )
            recycleMembers.adapter = membersAdapter

            shimmerMembers.animate().alpha(0f).setDuration(300).withEndAction {
                shimmerMembers.stopShimmer()
                shimmerMembers.animate().alpha(1f).setDuration(300)
                shimmerMembers.visibility = View.GONE
                recycleMembers.visibility = View.VISIBLE
            }

        }
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
