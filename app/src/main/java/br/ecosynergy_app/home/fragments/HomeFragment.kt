package br.ecosynergy_app.home.fragments

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import br.ecosynergy_app.R
import br.ecosynergy_app.home.HomeActivity
import br.ecosynergy_app.readings.ReadingsViewModel
import br.ecosynergy_app.teams.DashboardActivity
import br.ecosynergy_app.teams.viewmodel.TeamsViewModel
import br.ecosynergy_app.user.viewmodel.UserViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private val userViewModel: UserViewModel by activityViewModels()
    private val teamsViewModel: TeamsViewModel by activityViewModels()
    private val readingsViewModel: ReadingsViewModel by activityViewModels()

    private lateinit var lblFirstname: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var shimmerName: ShimmerFrameLayout

    private lateinit var spinnerTeam: Spinner
    private lateinit var spinnerPeriod: Spinner

    private lateinit var pieChart: PieChart

    private lateinit var recyclerDashboards: RecyclerView
    private lateinit var dashboardsAdapter: DashboardsAdapter

    private var accessToken: String = ""
    private var identifier: String = ""
    private var userId: Int = 0

    private var teamHandles: List<String> = emptyList()


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
        pieChart = view.findViewById(R.id.pieChart)

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

        setupPieChart()
    }

    private fun setupPieChart() {
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = false
        pieChart.setEntryLabelColor(android.R.color.white)

        pieChart.setEntryLabelTextSize(14f)
        pieChart.legend.textColor = getThemeColor(android.R.attr.textColorPrimary)

        pieChart.animateY(1000, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)

        val pieEntries = mutableListOf<PieEntry>()
        val label = ""

        val statsMap = mapOf("Propano(C3H8)" to 92.5f, "Dióxido de Carbono(CO2)" to 5.3f, "Outros" to 1.2f)

        statsMap.forEach { (key, value) ->
            pieEntries.add(PieEntry(value, key))
        }

        val pieDataSet = PieDataSet(pieEntries, label)

        pieDataSet.valueTextColor = getThemeColor(android.R.attr.textColorPrimary)

        pieDataSet.colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.blue),
            ContextCompat.getColor(requireContext(), R.color.greenDark),
            ContextCompat.getColor(requireContext(), R.color.green)
        )

        pieDataSet.sliceSpace = 2f
        pieDataSet.selectionShift = 5f

        val pieData = PieData(pieDataSet)
        pieData.setValueTextSize(12f)
        pieData.setValueTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))

        pieData.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${value.toInt()}%"
            }
        })

        pieChart.data = pieData

        pieChart.invalidate()
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
        lifecycleScope.launch {
            teamsViewModel.getAllTeamsFromDB().collectLatest { teamData ->
                teamHandles = teamData.map { it.handle }

                val teamsArrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, teamHandles)
                spinnerTeam.adapter = teamsArrayAdapter

                val dashboardList = teamData.map { team ->
                    DashboardItem(
                        id = team.id,
                        name = team.name,
                        handle = "@" + team.handle,
                        imageResourceId = HomeActivity().getDrawableForLetter(team.name.first())
                    )
                }

                dashboardsAdapter.updateList(dashboardList)
                recyclerDashboards.adapter = dashboardsAdapter
            }
        }
    }


    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = false
        }
    }

    private fun getThemeColor(attrResId: Int): Int {
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(attrResId, typedValue, true)
        return typedValue.data
    }
}