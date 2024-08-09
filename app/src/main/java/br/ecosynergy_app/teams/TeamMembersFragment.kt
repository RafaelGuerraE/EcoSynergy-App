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
import androidx.fragment.app.Fragment
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
import com.google.android.material.textfield.TextInputEditText
import de.hdodenhof.circleimageview.CircleImageView

class TeamMembersFragment : Fragment(R.layout.fragment_team_members) {
    private lateinit var recycleMembers: RecyclerView
    private lateinit var membersAdapter: MembersAdapter

    private lateinit var userViewModel: UserViewModel
    private lateinit var teamsViewModel: TeamsViewModel

    private lateinit var shimmerMembers: ShimmerFrameLayout

    private var token: String? = ""
    private var teamHandle: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_team_members, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        teamsViewModel = ViewModelProvider(this, TeamsViewModelFactory(RetrofitClient.teamsService))[TeamsViewModel::class.java]
        userViewModel = ViewModelProvider(this, UserViewModelFactory(RetrofitClient.userService))[UserViewModel::class.java]

        val sp: SharedPreferences = requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        token = sp.getString("accessToken", null)

        teamHandle = arguments?.getString("TEAM_HANDLE")
        Log.d("TeamOverviewFragment", "Team Handle: $teamHandle")

        shimmerMembers = view.findViewById(R.id.shimmerMembers)

        recycleMembers = view.findViewById(R.id.recycleMembers)

        recycleMembers.layoutManager = LinearLayoutManager(requireContext())
        membersAdapter = MembersAdapter(emptyList(), emptyList())
        recycleMembers.adapter = membersAdapter

        observeTeamInfo()
    }

    private fun observeMembersInfo(members: List<Member>){
        val memberIds = members.map { it.id.toString() }
        val memberRoles = members.map {it.role}

        userViewModel.getUsersByIds(memberIds, token)
        userViewModel.users.observe(viewLifecycleOwner) { result ->
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
}