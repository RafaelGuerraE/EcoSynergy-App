package br.ecosynergy_app.home.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import br.ecosynergy_app.R
import br.ecosynergy_app.home.HomeActivity
import br.ecosynergy_app.readings.ReadingsViewModel
import br.ecosynergy_app.teams.DashboardActivity
import br.ecosynergy_app.teams.TeamsViewModel
import br.ecosynergy_app.user.UserViewModel
import com.facebook.shimmer.ShimmerFrameLayout

class Home : Fragment() {

    private val userViewModel: UserViewModel by activityViewModels()
    private val teamsViewModel: TeamsViewModel by activityViewModels()
    private val readingsViewModel: ReadingsViewModel by activityViewModels()

    private lateinit var lblFirstname: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var shimmerName: ShimmerFrameLayout

    private lateinit var spinnerTeam: Spinner
    private lateinit var spinnerPeriod: Spinner

    private lateinit var recyclerDashboards: RecyclerView
    private lateinit var dashboardsAdapter: DashboardsAdapter

    private var accessToken: String = ""
    private var identifier: String = ""
    private var userId: Int = 0

    private var teamHandles: List<String> = listOf("Todas") + emptyList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        lblFirstname = view.findViewById(R.id.lblFirstname)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        shimmerName = view.findViewById(R.id.shimmerName)

        recyclerDashboards = view.findViewById(R.id.recyclerDashboards)
        recyclerDashboards.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        dashboardsAdapter = DashboardsAdapter(emptyList(), requireActivity()){ dashboardItem ->
            // Handle item click here. For example, navigate to a detailed view
            val i = Intent(requireActivity(), DashboardActivity::class.java).apply {
                putExtra("TEAM_ID", dashboardItem.id)
                putExtra("TEAM_NAME", dashboardItem.name)
                putExtra("TEAM_HANDLE", dashboardItem.handle)
                putExtra("TEAM_INITIAL", dashboardItem.imageResourceId)
                putExtra("ACCESS_TOKEN", accessToken)
            }
            startActivity(i)
        }
        recyclerDashboards.adapter = dashboardsAdapter

        spinnerTeam = view.findViewById(R.id.spinnerTeam)
        spinnerPeriod = view.findViewById(R.id.spinnerPeriod)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSwipeRefresh()
        observeUserData()
        observeTeamsData()
    }

    private fun observeUserData() {
        userViewModel.userInfo.observe(viewLifecycleOwner){user ->
            shimmerName.animate().alpha(0f).setDuration(300).withEndAction {
                shimmerName.stopShimmer()
                shimmerName.visibility = View.GONE
                lblFirstname.visibility = View.VISIBLE
                lblFirstname.animate().alpha(1f).setDuration(300)
            }
            val firstName = user?.fullName?.split(" ")?.firstOrNull()
            lblFirstname.text = "$firstName!"

            accessToken = user.accessToken
            identifier = user.username
            userId = user.id
        }
    }

    private fun observeTeamsData() {
        teamsViewModel.getAllTeamsFromDB()
        teamsViewModel.allTeamsDB.observe(viewLifecycleOwner) { teamData ->
            teamHandles = listOf("Todas") + teamData.map { it.handle }

            val teamsArrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, teamHandles)
            spinnerTeam.adapter = teamsArrayAdapter

            val dashboardList = teamData.map { team ->
                DashboardItem(
                    id = team.id,
                    name = team.name,
                    handle = team.handle,
                    imageResourceId = HomeActivity().getDrawableForLetter(team.name.first())
                )
            }

            dashboardsAdapter.updateList(dashboardList)
            recyclerDashboards.adapter = dashboardsAdapter
        }
        // teamsViewModel.allTeamsDB.removeObservers(viewLifecycleOwner)
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = false
        }
    }
}