package br.ecosynergy_app.home.homefragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.ecosynergy_app.R
import br.ecosynergy_app.login.ResetActivity
import br.ecosynergy_app.teams.TeamAdapter
import br.ecosynergy_app.teams.TeamOverviewActivity
import br.ecosynergy_app.teams.TeamsViewModel
import br.ecosynergy_app.teams.TeamsResponse

class Teams : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var teamsAdapter: TeamAdapter
    private val teamsViewModel: TeamsViewModel by activityViewModels()

    private lateinit var btnAddTeam: ImageButton
    private lateinit var linearAlert: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_teams, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        btnAddTeam = view.findViewById(R.id.btnAddTeam)
        linearAlert = view.findViewById(R.id.linearAlert)

        observeTeamsData(linearAlert)

        btnAddTeam.setOnClickListener{
            val i = Intent(requireContext(), TeamOverviewActivity::class.java)
            startActivity(i)
        }
    }

    private fun observeTeamsData(linearLayout: LinearLayout) {
        teamsViewModel.teamsResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { teamData ->
                if (teamData.isEmpty()) {
                    linearLayout.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    linearLayout.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    teamsAdapter = TeamAdapter(teamData)
                    recyclerView.adapter = teamsAdapter
                }
            }.onFailure { e ->
                Log.e("TeamsFragment", "Error", e)
            }
        }
    }

}
