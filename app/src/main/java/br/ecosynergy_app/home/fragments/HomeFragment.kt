package br.ecosynergy_app.home.fragments

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
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
import br.ecosynergy_app.teams.CreateTeamActivity
import br.ecosynergy_app.teams.DashboardActivity
import br.ecosynergy_app.teams.viewmodel.TeamsViewModel
import br.ecosynergy_app.user.viewmodel.UserViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
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

    private lateinit var shimmerTotal: ShimmerFrameLayout

    private lateinit var linearGoal: LinearLayout

    private lateinit var txtValue: TextView
    private lateinit var txtGoal: TextView
    private lateinit var txtPercentage: TextView
    private lateinit var imgPercentage: ImageView

    private lateinit var linearTeamChart: LinearLayout
    private lateinit var teamsChart: LineChart
    private lateinit var shimmerTeams: ShimmerFrameLayout


    private lateinit var txtAlert: LinearLayout
    private lateinit var linearTotal: LinearLayout

    private lateinit var recyclerDashboards: RecyclerView
    private lateinit var dashboardsAdapter: DashboardsAdapter

    private var selectedPeriod: String = "Diário"

    private var accessToken: String = ""
    private var identifier: String = ""
    private var userId: Int = 0

    private var selectedTeamHandle: String = ""

    private var teamHandles: List<String> = emptyList()
    private var isHandles: Boolean = false

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

        linearGoal = view.findViewById(R.id.linearGoal)

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
                val fragmentManager = parentFragmentManager
                val transaction = fragmentManager.beginTransaction()

                transaction.replace(R.id.frame_layout, TeamsFragment())
                    .addToBackStack(null)
                    .commit()
            }
        )

        recyclerDashboards.adapter = dashboardsAdapter

        shimmerTotal = view.findViewById(R.id.shimmerTotal)

        spinnerTeam = view.findViewById(R.id.spinnerTeam)
        spinnerPeriod = view.findViewById(R.id.spinnerPeriod)

        txtValue = view.findViewById(R.id.txtValue)
        txtGoal = view.findViewById(R.id.txtGoal)
        txtPercentage = view.findViewById(R.id.txtPercentage)
        imgPercentage = view.findViewById(R.id.imgPercentage)

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

                linearGoal.visibility = View.GONE
                readingsViewModel.isFetchComplete.observe(viewLifecycleOwner) { isComplete ->
                    if (isComplete) {
                        setupTeamsChart(selectedTeamHandle, isHandles)
                        teamsViewModel.getTeamByHandleFromDB(selectedTeamHandle) { setGoalReminder() }
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setGoalReminder() {

        spinnerPeriod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val selected = parent.getItemAtPosition(position).toString()
                selectedPeriod = selected
                when (selected) {
                    "Diário" -> updateDailyGoal()
                    "Semanal" -> updateWeeklyGoal()
                    "Mensal" -> updateMonthlyGoal()
                    "Anual" -> updateAnnualGoal()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

            updateDailyGoal()
    }

    private fun updateDailyGoal() {
        shimmerTotal.animate().alpha(1f).setDuration(100).withEndAction {
            shimmerTotal.visibility = View.VISIBLE
            linearGoal.visibility = View.GONE
            linearGoal.animate().alpha(0f).setDuration(100)
        }
        val teamInfo = teamsViewModel.teamDB.value
        readingsViewModel.getAggregatedReadingsForDate(selectedTeamHandle, Date()) {
            readingsViewModel.aggregatedReadingsForDate.observe(viewLifecycleOwner) { readings ->
                if (teamInfo != null) {
                    handleGoalUpdate(readings.values.sum(), teamInfo.dailyGoal)
                }
                Log.d("HomeFragment", "$readings")
            }
        }
    }

    private fun updateWeeklyGoal() {
        shimmerTotal.animate().alpha(1f).setDuration(100).withEndAction {
            shimmerTotal.visibility = View.VISIBLE
            linearGoal.visibility = View.GONE
            linearGoal.animate().alpha(0f).setDuration(100)
        }
        val teamInfo = teamsViewModel.teamDB.value
        readingsViewModel.getAggregatedReadingsForLastWeek(selectedTeamHandle) {
            readingsViewModel.aggregatedReadingsForLastWeek.observe(viewLifecycleOwner) { readings ->
                if (teamInfo != null) {
                    handleGoalUpdate(readings.values.sum(), teamInfo.weeklyGoal)
                }
                Log.d("HomeFragment", "$readings")
            }
        }
    }

    private fun updateMonthlyGoal() {
        shimmerTotal.animate().alpha(1f).setDuration(100).withEndAction {
            shimmerTotal.visibility = View.VISIBLE
            linearGoal.visibility = View.GONE
            linearGoal.animate().alpha(0f).setDuration(100)
        }
        val teamInfo = teamsViewModel.teamDB.value
        readingsViewModel.getAggregatedReadingsForMonth(selectedTeamHandle) {
            readingsViewModel.aggregatedReadingsForMonth.observe(viewLifecycleOwner) { readings ->
                if (teamInfo != null) {
                    handleGoalUpdate(readings.values.sum(), teamInfo.monthlyGoal)
                }
                Log.d("HomeFragment", "$readings")
            }
        }
    }

    private fun updateAnnualGoal() {
        shimmerTotal.animate().alpha(1f).setDuration(100).withEndAction {
            shimmerTotal.visibility = View.VISIBLE
            linearGoal.visibility = View.GONE
            linearGoal.animate().alpha(0f).setDuration(100)
        }
        val teamInfo = teamsViewModel.teamDB.value
        readingsViewModel.getAggregatedReadingsForYear(selectedTeamHandle) {
            readingsViewModel.aggregatedReadingsForYear.observe(viewLifecycleOwner) { readings ->
                if (teamInfo != null) {
                    handleGoalUpdate(readings.values.sum(), teamInfo.annualGoal)
                }

                Log.d("HomeFragment", "$readings")
            }
        }
    }

    private fun handleGoalUpdate(totalEmissions: Float, goal: Double) {
        Log.d("HomeFragment", "$totalEmissions $goal")

        txtValue.text = formatGoal(totalEmissions.toDouble()) + " /"
        txtGoal.text = formatGoal(goal)

        if (goal != 0.0) {
            val percentage = (totalEmissions / goal) * 100
            updatePercentage(percentage)
        } else {
            resetPercentage()
        }

        shimmerTotal.animate().alpha(0f).setDuration(300).withEndAction {
            shimmerTotal.visibility = View.GONE
            linearGoal.visibility = View.VISIBLE
            linearGoal.animate().alpha(1f).setDuration(300)
        }
    }

    private fun formatGoal(value: Double): String {
        return when {
            value < 1000 -> value.toInt().toString()
            value < 1_000_000 -> "${(value / 1000).toInt()} mil"
            value < 1_000_000_000 -> "${(value / 1_000_000).toInt()} mi."
            else -> "${(value / 1_000_000_000).toInt()} bi."
        }
    }

    private fun updatePercentage(percentage: Double) {
        txtPercentage.text = String.format("%.2f%%", percentage)
        imgPercentage.visibility = View.VISIBLE

        when {
            percentage < 60.00 -> {
                imgPercentage.setImageResource(R.drawable.ic_decrease)
                imgPercentage.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.green),
                    PorterDuff.Mode.SRC_IN
                )
            }

            percentage < 100.00 -> {
                imgPercentage.setImageResource(R.drawable.ic_decrease)
                imgPercentage.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.orange),
                    PorterDuff.Mode.SRC_IN
                )
            }

            percentage >= 100.00 -> {
                imgPercentage.setImageResource(R.drawable.ic_rise)
                imgPercentage.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.red),
                    PorterDuff.Mode.SRC_IN
                )
            }
        }
    }

    private fun resetPercentage() {
        txtPercentage.text = "N/A"
        imgPercentage.visibility = View.GONE
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
                        listOf("Não há equipes")
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

                    isHandles = false

                    txtAlert.visibility = View.VISIBLE
                    linearTotal.visibility = View.GONE
                    shimmerTotal.visibility = View.GONE
                    linearTeamChart.visibility = View.GONE

                } else {
                    spinnerTeam.isClickable = true
                    spinnerTeam.isEnabled = true

                    isHandles = true

                    txtAlert.visibility = View.GONE
                    linearTotal.visibility = View.VISIBLE
                    shimmerTotal.visibility = View.VISIBLE
                    linearTeamChart.visibility = View.VISIBLE
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
            teamsViewModel.getTeamByHandleFromDB(selectedTeamHandle) {}
            teamsViewModel.getTeamsByUserId(userId, accessToken) {
                lifecycleScope.launch {
                    teamsViewModel.getAllTeamsFromDB().collect { teamData ->
                        val teamIds = teamData.map { it.id }
                        for (id in teamIds) {
                            teamsViewModel.findInvitesByTeam(id, accessToken)
                        }

                        val teamHandles = teamData.map { it.handle }
                        fetchReadingsData(teamHandles, accessToken) {
                            setupTeamsChart(selectedTeamHandle, isHandles)

                            swipeRefresh.isRefreshing = false

                            when(selectedPeriod){
                                "Diário" -> updateDailyGoal()
                                "Semanal" -> updateWeeklyGoal()
                                "Mensal" -> updateMonthlyGoal()
                                "Anual" -> updateAnnualGoal()
                                else -> {}
                            }
                        }

                    }
                }
            }
        }
    }

    private fun fetchReadingsData(listTeamHandles: List<String>, accessToken: String, onComplete: () -> Unit) {
        readingsViewModel.fetchAllReadings(listTeamHandles, accessToken) {
            onComplete()
        }
    }

    private fun getThemeColor(attrResId: Int): Int {
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(attrResId, typedValue, true)
        return typedValue.data
    }

    private fun setupTeamsChart(teamHandle: String, isHandles: Boolean) {
        if (!isHandles) {
            //showToast("Não existem equipes")
        } else {
            shimmerTeams.animate().alpha(1f).setDuration(100).withEndAction {
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
                    calendar.add(Calendar.DAY_OF_YEAR, -6 + i)
                    dateLabels.add(dateFormat.format(calendar.time))
                }

                val lineEntries = mutableListOf<Entry>()
                val tempCalendar = Calendar.getInstance()

                for (i in 0 until 7) {
                    tempCalendar.time = Date()
                    tempCalendar.add(Calendar.DAY_OF_YEAR, -6 + i)

                    val dateKey = SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                    ).format(tempCalendar.time)
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
                    axisMinimum = 0f
                    textColor = getThemeColor(android.R.attr.textColorPrimary)
                }
                teamsChart.axisRight.isEnabled = false
                teamsChart.description.isEnabled = false
                teamsChart.legend.textColor = getThemeColor(android.R.attr.textColorPrimary)

                val lineData = LineData(lineDataSet)
                teamsChart.data = lineData
                teamsChart.invalidate()

                shimmerTeams.animate().alpha(0f).setDuration(300).withEndAction {
                    shimmerTeams.visibility = View.GONE
                    teamsChart.visibility = View.VISIBLE
                    teamsChart.animate().alpha(1f).setDuration(300)
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}