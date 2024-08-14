package br.ecosynergy_app.teams

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.user.MembersAdapter
import br.ecosynergy_app.user.UserResponse
import br.ecosynergy_app.user.UserViewModel
import br.ecosynergy_app.user.UserViewModelFactory
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class TeamMembersFragment : Fragment(R.layout.fragment_team_members) {
    private lateinit var recycleMembers: RecyclerView
    lateinit var membersAdapter: MembersAdapter
    private lateinit var shimmerMembers: ShimmerFrameLayout

    private lateinit var userViewModel: UserViewModel
    private lateinit var teamsViewModel: TeamsViewModel

    private lateinit var swipeRefresh: SwipeRefreshLayout

    lateinit var txtMember: TextInputEditText
    lateinit var btnAddMember: ImageButton

    private var token: String? = ""
    private var teamHandle: String? = ""
    private var teamId: String? = ""

    private var membersList: List<UserResponse> = listOf()

    private var memberIds: MutableList<String> = mutableListOf()
    private var memberRoles: List<String> = listOf()
    private var currentUserRole: String? = ""
    private var userId: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_team_members, container, false)

        teamsViewModel = ViewModelProvider(this, TeamsViewModelFactory(RetrofitClient.teamsService))[TeamsViewModel::class.java]
        userViewModel = ViewModelProvider(this, UserViewModelFactory(RetrofitClient.userService))[UserViewModel::class.java]

        val sp: SharedPreferences = requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        token = sp.getString("accessToken", null)
        userId = sp.getString("id", null)

        teamHandle = arguments?.getString("TEAM_HANDLE")
        teamId = arguments?.getString("TEAM_ID")

        shimmerMembers = view.findViewById(R.id.shimmerMembers)
        recycleMembers = view.findViewById(R.id.recycleMembers)

        txtMember = view.findViewById(R.id.txtMember)
        btnAddMember = view.findViewById(R.id.btnAddMember)

        swipeRefresh = view.findViewById(R.id.swipeRefresh)

        recycleMembers.layoutManager = LinearLayoutManager(requireContext())
        membersAdapter = MembersAdapter(emptyList(), emptyList(), currentUserRole, teamId, teamHandle, teamsViewModel, requireActivity(), this, memberIds)
        recycleMembers.adapter = membersAdapter

        return  view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeTeamInfo()
        setupSwipeRefresh()

        parentFragmentManager.setFragmentResultListener("requestKey", this) { key, bundle ->
            if (key == "requestKey") {
                shimmerMembers.startShimmer()
                shimmerMembers.visibility = View.VISIBLE
                recycleMembers.visibility = View.GONE
                observeTeamInfo()
            }
        }

        txtMember.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterMembers(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        btnAddMember.setOnClickListener{
            val memberIdsString = memberIds.joinToString(",")
            val addMembersBottomSheet = AddMembersBottomSheet().apply {
                arguments = Bundle().apply {
                    putString("TEAM_HANDLE", teamHandle)
                    putString("TEAM_ID", teamId)
                    putString("MEMBER_IDS", memberIdsString)
                }
            }
            addMembersBottomSheet.show(parentFragmentManager, "AddMembersBottomSheet")
            Log.d("btnAddMember", "MemberIDS: $memberIdsString, $teamHandle, $teamId")
        }
    }

    private fun observeMembersInfo(members: List<Member>) {
        memberIds = members.map { it.id.toString() }.toMutableList()
        memberIds = memberIds.toMutableList() as ArrayList<String>
        memberRoles = members.map { it.role }

        teamsViewModel.teamResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                val membersResponse = response.members
                val userMember = membersResponse.find { it.id.toString() == userId }
                currentUserRole = userMember?.role
                userViewModel.getUsersByIds(memberIds, token)
            }.onFailure { error ->
                error.printStackTrace()
                Log.d("TeamOverviewFragment", "Team Result Failed: ${error.message}")
            }
        }

        userViewModel.users.observe(viewLifecycleOwner) { result ->
            result.onSuccess { users ->
                shimmerMembers.visibility = View.VISIBLE
                recycleMembers.visibility = View.GONE

                val pairedMembers = users.map { user ->
                    val role = members.find { it.id.toString() == user.id.toString() }?.role ?: "Unknown"
                    Pair(user, role)
                }.sortedBy { it.first.fullName }

                val sortedUsers = pairedMembers.map { it.first }
                val sortedRoles = pairedMembers.map { it.second }

                membersAdapter = MembersAdapter(sortedUsers, sortedRoles, currentUserRole, teamId, teamHandle, teamsViewModel, requireActivity(), this, memberIds)
                recycleMembers.adapter = membersAdapter

                shimmerMembers.animate().alpha(0f).setDuration(300).withEndAction {
                    shimmerMembers.stopShimmer()
                    shimmerMembers.animate().alpha(1f).setDuration(300)
                    shimmerMembers.visibility = View.GONE
                    recycleMembers.visibility = View.VISIBLE
                }
            }.onFailure { error ->
                error.printStackTrace()
                Log.d("TeamOverviewFragment", "User Result Failed: ${error.message}")
                shimmerMembers.visibility = View.VISIBLE
                recycleMembers.visibility = View.GONE
            }
        }
    }


    private fun observeTeamInfo(){
        teamsViewModel.findTeamByHandle(token, teamHandle)
        teamsViewModel.teamResult.observe(viewLifecycleOwner){ result->
            result.onSuccess { response ->
                observeMembersInfo(response.members)
            }.onFailure { error ->
                error.printStackTrace()
                Log.d("TeamOverviewFragment", "Team Result Failed: ${error.message}")
            }
        }
    }

    private fun filterMembers(query: String) {
        val filteredList = membersList.filter {
            it.fullName.contains(query, ignoreCase = true) || it.username.contains(query, ignoreCase = true)
        }
        membersAdapter.updateList(filteredList, memberRoles)
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

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            observeTeamInfo()
            swipeRefresh.isRefreshing = false
        }
    }
}