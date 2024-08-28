package br.ecosynergy_app.home.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import br.ecosynergy_app.R
import br.ecosynergy_app.teams.CreateTeamBottomSheet
import br.ecosynergy_app.teams.TeamAdapter
import br.ecosynergy_app.teams.TeamsViewModel

class Teams : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var teamsAdapter: TeamAdapter
    private val teamsViewModel: TeamsViewModel by activityViewModels()

    private lateinit var btnAddTeam: ImageButton
    private lateinit var linearAlert: LinearLayout

    private lateinit var swipeRefresh: SwipeRefreshLayout

    private var token: String? = ""
    private var identifier: String? = ""
    private var userId: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_teams, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        teamsAdapter = TeamAdapter(emptyList())

        btnAddTeam = view.findViewById(R.id.btnAddTeam)
        linearAlert = view.findViewById(R.id.linearAlert)

        linearAlert.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        swipeRefresh = view.findViewById(R.id.swipeRefresh)

        val sp: SharedPreferences = requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        token = sp.getString("accessToken", null)
        identifier = sp.getString("identifier", null)
        userId = sp.getString("id", null)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getTeamsByUserId()
        observeTeamsData()
        btnAddTeam.setOnClickListener{
            val createTeamBottomSheet = CreateTeamBottomSheet()
            createTeamBottomSheet.show(parentFragmentManager, "CreateTeamBottomSheet")
        }
        setupSwipeRefresh()
    }

    private fun getTeamsByUserId(){
        teamsViewModel.findTeamsByUserId(userId, token)
        teamsViewModel.teamsResult.removeObservers(this)
        observeTeamsData()
   }

    private fun observeTeamsData() {
        teamsViewModel.teamsResult.observe(viewLifecycleOwner) { result ->
            Log.d("TeamsFragment", "TeamsResult: $result")
            result.onSuccess { teamData ->
                teamsAdapter = TeamAdapter(teamData)
                recyclerView.adapter = teamsAdapter
                if (teamData.isNotEmpty()) {
                    linearAlert.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }.onFailure { e ->
                Log.e("TeamsFragment", "Error", e)
            }
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = false
        }
    }

}
