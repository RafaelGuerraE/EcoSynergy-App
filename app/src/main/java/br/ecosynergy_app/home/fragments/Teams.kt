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
import android.widget.ProgressBar
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

    private lateinit var progressBar: ProgressBar

    private lateinit var swipeRefresh: SwipeRefreshLayout

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

        progressBar = view.findViewById(R.id.loadingProgressBar)

        linearAlert.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE

        swipeRefresh = view.findViewById(R.id.swipeRefresh)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeTeamsData()
        btnAddTeam.setOnClickListener {
            val createTeamBottomSheet = CreateTeamBottomSheet()
            createTeamBottomSheet.show(parentFragmentManager, "CreateTeamBottomSheet")
        }
        setupSwipeRefresh()
    }

    private fun observeTeamsData() {
        teamsViewModel.getAllTeamsFromDB()
        teamsViewModel.allTeamsDB.observe(viewLifecycleOwner) { teamData ->
            teamsAdapter = TeamAdapter(teamData)
            recyclerView.adapter = teamsAdapter
            if (teamData.isEmpty()) {
                linearAlert.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
            progressBar.visibility = View.GONE
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = false
        }
    }

}
