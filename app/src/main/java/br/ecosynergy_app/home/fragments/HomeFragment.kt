package br.ecosynergy_app.home.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
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
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.readings.ReadingsRepository
import br.ecosynergy_app.teams.CreateTeamActivity
import br.ecosynergy_app.teams.DashboardActivity
import br.ecosynergy_app.teams.viewmodel.TeamsViewModel
import br.ecosynergy_app.user.viewmodel.UserViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private val userViewModel: UserViewModel by activityViewModels()
    private val teamsViewModel: TeamsViewModel by activityViewModels()
    private val readingsViewModel: ReadingsViewModel by activityViewModels()

    private lateinit var lblFirstname: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var shimmerName: ShimmerFrameLayout

    private lateinit var spinnerTeam: Spinner
    private lateinit var spinnerPeriod: Spinner

    private lateinit var linearTeamChart: LinearLayout
    private lateinit var teamsChart: LineChart
    private lateinit var shimmerTeams: ShimmerFrameLayout


    private lateinit var txtAlert: LinearLayout
    private lateinit var linearTotal: LinearLayout

    private lateinit var recyclerDashboards: RecyclerView
    private lateinit var dashboardsAdapter: DashboardsAdapter

    private var accessToken: String = ""
    private var identifier: String = ""
    private var userId: Int = 0

    private var selectedTeamHandle: String = ""

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

        linearTeamChart = view.findViewById(R.id.linearTeamChart)
        teamsChart = view.findViewById(R.id.teamsChart)
        shimmerTeams = view.findViewById(R.id.shimmerTeams)

        txtAlert = view.findViewById(R.id.txtAlert)
        linearTotal = view.findViewById(R.id.linearTotal)

        recyclerDashboards = view.findViewById(R.id.recyclerDashboards)
        recyclerDashboards.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        dashboardsAdapter = DashboardsAdapter(emptyList(), requireActivity(),
            onItemClick = { dashboardItem ->
                val i = Intent(requireActivity(), DashboardActivity::class.java).apply {
                    putExtra("TEAM_ID", dashboardItem.id)
                    putExtra("TEAM_NAME", dashboardItem.name)
                    putExtra("TEAM_HANDLE", dashboardItem.handle)
                    putExtra("TEAM_INITIAL", dashboardItem.imageResourceId)
                    putExtra("ACCESS_TOKEN", accessToken)
                }
                startActivity(i)
            },
            onCreateTeamClick = {
                val i = Intent(requireContext(), CreateTeamActivity::class.java)
                i.apply {
                    putExtra("USER_ID", userId)
                    putExtra("ACCESS_TOKEN", accessToken)
                }
                startActivity(i)
            },
            onAllTeamsRedirectClick = {

            }
        )

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

        spinnerTeam.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedTeamHandle = parent.getItemAtPosition(position) as String
                setupTeamsChart(selectedTeamHandle)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    private fun observeUserData() {
        userViewModel.userInfo.observe(viewLifecycleOwner) { user ->
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

                val teamsArrayAdapter = if (teamHandles.isEmpty()) {
                    ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        listOf("Não há equipe")
                    )
                } else {
                    ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        teamHandles
                    )
                }

                if (teamHandles.isEmpty()) {
                    spinnerTeam.isClickable = false
                    spinnerTeam.isEnabled = false

                    txtAlert.visibility = View.VISIBLE
                    linearTotal.visibility = View.GONE

                } else {
                    spinnerTeam.isClickable = true
                    spinnerTeam.isEnabled = true


                    txtAlert.visibility = View.GONE
                    linearTotal.visibility = View.VISIBLE
                }

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
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            teamsViewModel.getTeamsByUserId(userId, accessToken) {
                lifecycleScope.launch {
                    teamsViewModel.getAllTeamsFromDB().collect { teamData ->
                        val teamIds = teamData.map { it.id }
                        for (id in teamIds) {
                            teamsViewModel.findInvitesByTeam(id, accessToken)
                        }

                        val teamHandles = teamData.map {it.handle}
                        fetchReadingsData(teamHandles, accessToken){
                            setupTeamsChart(selectedTeamHandle)
                        }

                    }
                }
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun fetchReadingsData(listTeamHandles: List<String>, accessToken: String, onComplete: () -> Unit) {
        for (teamHandle in listTeamHandles) {
            readingsViewModel.updateMQ7Readings(teamHandle, accessToken)
            readingsViewModel.updateMQ135Readings(teamHandle, accessToken)
            readingsViewModel.updateFireReadings(teamHandle, accessToken)
        }

        onComplete()
    }

    private fun getThemeColor(attrResId: Int): Int {
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(attrResId, typedValue, true)
        return typedValue.data
    }


    private fun setupTeamsChart(teamHandle: String) {
        shimmerTeams.animate().alpha(1f).setDuration(100).withEndAction {
            shimmerTeams.startShimmer()
            shimmerTeams.visibility = View.VISIBLE
            teamsChart.visibility = View.GONE
            teamsChart.animate().alpha(0f).setDuration(100)
        }

        val dateLabels = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

        readingsViewModel.getAggregatedReadingsForLastWeek(teamHandle) {
            val emissionsData = readingsViewModel.aggregatedReadingsForLastWeek.value

            for (i in 0..6) {
                calendar.time = Date()
                calendar.add(Calendar.DAY_OF_YEAR, -6 + i) // Adjusting to get the correct date
                dateLabels.add(dateFormat.format(calendar.time))
            }

            val lineEntries = mutableListOf<Entry>()
            val tempCalendar = Calendar.getInstance()

            for (i in 0 until 7) {
                tempCalendar.time = Date()
                tempCalendar.add(Calendar.DAY_OF_YEAR, -6 + i)

                val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(tempCalendar.time)
                val value = emissionsData?.get(dateKey) ?: 0f

                lineEntries.add(Entry(i.toFloat(), value))
            }

            val lineDataSet = LineDataSet(lineEntries, "Total de Emissões").apply {
                color = ContextCompat.getColor(requireContext(), R.color.blue)
                valueTextColor = getThemeColor(android.R.attr.textColorPrimary)
                valueTextSize = 10f
                setDrawValues(true)
                lineWidth = 3f
                setDrawFilled(true)
                setDrawCircles(true)
                fillColor = ContextCompat.getColor(requireContext(), R.color.blue_50)
                setCircleColor(ContextCompat.getColor(requireContext(), R.color.blue))
                circleRadius = 5f
                valueFormatter = DefaultValueFormatter(0)
            }

            teamsChart.xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = getThemeColor(android.R.attr.textColorPrimary)
                granularity = 1f
                labelCount = dateLabels.size
                valueFormatter = IndexAxisValueFormatter(dateLabels)
            }

            teamsChart.axisLeft.apply {
                setDrawGridLines(false)
                textColor = getThemeColor(android.R.attr.textColorPrimary)
            }
            teamsChart.axisRight.isEnabled = false
            teamsChart.description.isEnabled = false
            teamsChart.legend.textColor = getThemeColor(android.R.attr.textColorPrimary)

            val lineData = LineData(lineDataSet)
            teamsChart.data = lineData
            teamsChart.invalidate()

            shimmerTeams.animate().alpha(0f).setDuration(300).withEndAction {
                shimmerTeams.stopShimmer()
                shimmerTeams.visibility = View.GONE
                teamsChart.visibility = View.VISIBLE
                teamsChart.animate().alpha(1f).setDuration(300)
            }
        }
    }


}