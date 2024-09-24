package br.ecosynergy_app.home.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import br.ecosynergy_app.R
import br.ecosynergy_app.user.UserViewModel
import br.ecosynergy_app.readings.ReadingVO
import br.ecosynergy_app.readings.ReadingsViewModel
import br.ecosynergy_app.room.Readings
import br.ecosynergy_app.teams.TeamsViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.Locale

class Home : Fragment() {

    private lateinit var lblFirstname: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var shimmerName: ShimmerFrameLayout

    private lateinit var spinnerTeam: Spinner
    private lateinit var spinnerPeriod: Spinner

    private lateinit var mq7Chart: LineChart
    private lateinit var shimmerMQ7: ShimmerFrameLayout

    private lateinit var mq135Chart: LineChart
    private lateinit var shimmerMQ135: ShimmerFrameLayout

    private lateinit var fireChart: LineChart
    private lateinit var shimmerFire: ShimmerFrameLayout

    private lateinit var btnShare: ImageButton

    private val userViewModel: UserViewModel by activityViewModels()
    private val teamsViewModel: TeamsViewModel by activityViewModels()
    private val readingsViewModel: ReadingsViewModel by activityViewModels()

    private var token: String = ""
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

        shimmerMQ7 = view.findViewById(R.id.shimmerMQ7)
        mq7Chart = view.findViewById(R.id.mq7Chart)

        shimmerMQ135 = view.findViewById(R.id.shimmerMQ135)
        mq135Chart = view.findViewById(R.id.mq135Chart)

        shimmerFire = view.findViewById(R.id.shimmerFire)
        fireChart = view.findViewById(R.id.fireChart)

        spinnerTeam = view.findViewById(R.id.spinnerTeam)
        spinnerPeriod = view.findViewById(R.id.spinnerPeriod)

        btnShare = view.findViewById(R.id.btnShare)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSwipeRefresh()
        observeUserData()
        observeTeamsData()

        btnShare.setOnClickListener{
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Relatório da Semana X de Setembro")
                type = "text/plain"
            }

            startActivity(Intent.createChooser(shareIntent, "Compartilhar relatório via"))
        }
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

            token = user.accessToken
            identifier = user.username
            userId = user.id
        }
    }

    private fun observeTeamsData(){
        teamsViewModel.getAllTeamsFromDB()
        teamsViewModel.allTeamsDB.observe(viewLifecycleOwner) { teamData ->
            teamHandles = listOf("Todas") + teamData.map { it.handle }

            val teamsArrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, teamHandles)
            spinnerTeam.adapter = teamsArrayAdapter

            fetchMQ7ReadingsByTeamHandle()
            fetchMQ135ReadingsByTeamHandle()
            fetchFireReadingsByTeamHandle()

           // teamsViewModel.allTeamsDB.removeObservers(viewLifecycleOwner)
        }
    }


    private fun fetchMQ7ReadingsByTeamHandle() {
        readingsViewModel.mq7ReadingsDB.observe(viewLifecycleOwner) { response ->
            handleMQ7Readings(response)
            Log.d("HomeFragment", "Sensors MQ7 OK")
            readingsViewModel.mq7ReadingsDB.removeObservers(viewLifecycleOwner)
        }
    }

    private fun fetchMQ135ReadingsByTeamHandle() {
        readingsViewModel.mq135ReadingsDB.observe(viewLifecycleOwner) { response ->
            handleMQ135Readings(response)
            Log.d("HomeFragment", "Sensors MQ135 OK")
            readingsViewModel.mq135ReadingsDB.removeObservers(viewLifecycleOwner)
        }
    }

    private fun fetchFireReadingsByTeamHandle() {
        readingsViewModel.fireReadingsDB.observe(viewLifecycleOwner) { response ->
            handleFireReadings(response)
            Log.d("HomeFragment", "FireSensors OK")
            readingsViewModel.mq7ReadingsDB.removeObservers(viewLifecycleOwner)
        }
    }

    private fun getThemeColor(attrResId: Int): Int {
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(attrResId, typedValue, true)
        return typedValue.data
    }

    private fun setupMQ7Chart(mq7Readings: List<Readings>) {
        mq7Chart.visibility = View.GONE
        shimmerMQ7.visibility = View.VISIBLE

        Log.d("HomeFragment", "Readings: $mq7Readings")

        val dateFormatIn = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val dateFormatOut = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val aggregatedData = mutableMapOf<String, Int>()

        mq7Readings.forEach { reading ->
            val parsedDate = dateFormatIn.parse(reading.timestamp)
            val date = if (parsedDate != null) dateFormatOut.format(parsedDate) else "Invalid Date"
            aggregatedData[date] = aggregatedData.getOrDefault(date, 0) + 1
        }

        //Log.d("HomeFragment", "Aggregated Data: $aggregatedData")

        val sortedData = aggregatedData.entries.sortedBy { it.key }

        val entries = ArrayList<Entry>()
        sortedData.forEachIndexed { index, entry ->
            entries.add(Entry(index.toFloat(), entry.value.toFloat()))
        }

        Log.d("HomeFragment", "Entries: $entries")

        val dataSet = LineDataSet(entries, "Nº de alertas por dia").apply {
            color = ContextCompat.getColor(requireContext(), R.color.greenDark)
            valueTextColor = getThemeColor(android.R.attr.textColorPrimary)
            valueTextSize = 10f
            setDrawValues(true)
            lineWidth = 3f
            setDrawCircles(true)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.greenDark))
            circleRadius = 5f
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(requireContext(), R.color.greenDark_50)
            valueFormatter = DefaultValueFormatter(0)
        }

        mq7Chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(sortedData.map { it.key })
            setDrawGridLines(false)
            textColor = getThemeColor(android.R.attr.textColorPrimary)
            setLabelCount(sortedData.size, true)
            labelRotationAngle = 90f
        }

        mq7Chart.axisLeft.apply {
            setDrawGridLines(false)
            textColor = getThemeColor(android.R.attr.textColorPrimary)
        }

        mq7Chart.axisRight.isEnabled = false
        mq7Chart.description.isEnabled = false
        mq7Chart.legend.textColor = getThemeColor(android.R.attr.textColorPrimary)

        mq7Chart.apply {
            isDragEnabled = true
            isScaleXEnabled = true
            setExtraOffsets(10f, 10f, 10f, 10f)
        }

        val lineData = LineData(dataSet)
        mq7Chart.data = lineData
        mq7Chart.invalidate()

        shimmerMQ7.animate().alpha(0f).setDuration(300).withEndAction {
            shimmerMQ7.stopShimmer()
            shimmerMQ7.visibility = View.GONE
            mq7Chart.visibility = View.VISIBLE
            mq7Chart.animate().alpha(1f).setDuration(300)
        }
    }

    private fun setupMQ135Chart(mq135Readings: List<Readings>) {
        mq135Chart.visibility = View.GONE
        shimmerMQ135.visibility = View.VISIBLE

        Log.d("HomeFragment", "Readings: $mq135Readings")

        val dateFormatIn = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val dateFormatOut = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val aggregatedData = mutableMapOf<String, Int>()

        mq135Readings.forEach { reading ->
            val parsedDate = dateFormatIn.parse(reading.timestamp)
            val date = if (parsedDate != null) dateFormatOut.format(parsedDate) else "Invalid Date"
            aggregatedData[date] = aggregatedData.getOrDefault(date, 0) + 1
        }

        Log.d("HomeFragment", "Aggregated Data: $aggregatedData")

        val sortedData = aggregatedData.entries.sortedBy { it.key }

        val entries = ArrayList<Entry>()
        sortedData.forEachIndexed { index, entry ->
            entries.add(Entry(index.toFloat(), entry.value.toFloat()))
        }

        Log.d("HomeFragment", "Entries: $entries")

        val dataSet = LineDataSet(entries, "Nº de alertas por dia").apply {
            color = ContextCompat.getColor(requireContext(), R.color.yellow)
            valueTextColor = getThemeColor(android.R.attr.textColorPrimary)
            valueTextSize = 10f
            setDrawValues(true)
            lineWidth = 3f
            setDrawCircles(true)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.yellow))
            circleRadius = 5f
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(requireContext(), R.color.yellow)
            valueFormatter = DefaultValueFormatter(0)
        }

        mq135Chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(sortedData.map { it.key })
            setDrawGridLines(false)
            setDrawLabels(true)
            textColor = getThemeColor(android.R.attr.textColorPrimary)
            setLabelCount(sortedData.size, true)
            labelRotationAngle = 90f
        }

        mq135Chart.axisLeft.apply {
            setDrawGridLines(false)
            textColor = getThemeColor(android.R.attr.textColorPrimary)
        }

        mq135Chart.axisRight.isEnabled = false
        mq135Chart.description.isEnabled = false
        mq135Chart.legend.textColor = getThemeColor(android.R.attr.textColorPrimary)

        mq135Chart.apply {
            isDragEnabled = true
            setScaleEnabled(true)
            isScaleXEnabled = true
            isScaleYEnabled = false // Disable vertical scaling if not needed
            setVisibleXRangeMaximum(5f) // Display only 5 entries at a time
            setExtraOffsets(10f, 10f, 10f, 10f)
            moveViewToX(0f) // Start chart view at the beginning
        }

        val lineData = LineData(dataSet)
        mq135Chart.data = lineData
        mq135Chart.invalidate()

        shimmerMQ135.animate().alpha(0f).setDuration(300).withEndAction {
            shimmerMQ135.stopShimmer()
            shimmerMQ135.visibility = View.GONE
            mq135Chart.visibility = View.VISIBLE
            mq135Chart.animate().alpha(1f).setDuration(300)
        }
    }


    private fun setupFireChart(fireReadings: List<Readings>) {
        fireChart.visibility = View.GONE

        shimmerFire.visibility = View.VISIBLE

        Log.d("HomeFragment", "Readings: $fireReadings")

        val dateFormatIn = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val dateFormatOut = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val aggregatedData = mutableMapOf<String, Int>()

        fireReadings.forEach { reading ->
            val parsedDate = dateFormatIn.parse(reading.timestamp)
            val date = if (parsedDate != null) dateFormatOut.format(parsedDate) else "Invalid Date"
            aggregatedData[date] = aggregatedData.getOrDefault(date, 0) + 1
        }

        Log.d("HomeFragment", "Aggregated Data: $aggregatedData")

        val sortedData = aggregatedData.entries.sortedBy { it.key }

        val entries = ArrayList<Entry>()
        sortedData.forEachIndexed { index, entry ->
            entries.add(Entry(index.toFloat(), entry.value.toFloat()))
        }

        Log.d("HomeFragment", "Entries: $entries")

        val dataSet = LineDataSet(entries, "Nº de alertas por dia").apply {
            color = ContextCompat.getColor(requireContext(), R.color.red)
            valueTextColor = getThemeColor(android.R.attr.textColorPrimary)
            valueTextSize = 10f
            setDrawValues(true)
            lineWidth = 3f
            setDrawCircles(true)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.red))
            circleRadius = 5f
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(requireContext(), R.color.red)
            valueFormatter = DefaultValueFormatter(0)
        }

        fireChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(sortedData.map { it.key })
            setDrawGridLines(false)
            textColor = getThemeColor(android.R.attr.textColorPrimary)
            setLabelCount(sortedData.size, true)
            labelRotationAngle = 90f
        }

        fireChart.axisLeft.apply {
            setDrawGridLines(false)
            textColor = getThemeColor(android.R.attr.textColorPrimary)
        }

        fireChart.axisRight.isEnabled = false
        fireChart.description.isEnabled = false
        fireChart.legend.textColor = getThemeColor(android.R.attr.textColorPrimary)

        fireChart.apply {
            isDragEnabled = true
            isScaleXEnabled = true
            setExtraOffsets(10f, 10f, 10f, 10f)
        }

        val lineData = LineData(dataSet)
        fireChart.data = lineData
        fireChart.invalidate()

        shimmerFire.animate().alpha(0f).setDuration(300).withEndAction {
            shimmerFire.stopShimmer()
            shimmerFire.visibility = View.GONE
            fireChart.visibility = View.VISIBLE
            fireChart.animate().alpha(1f).setDuration(300)
        }
    }

    private fun handleMQ7Readings(response: List<Readings>) {
        setupMQ7Chart(response)
    }

    private fun handleMQ135Readings(response: List<Readings>) {
        setupMQ135Chart(response)
    }

    private fun handleFireReadings(response: List<Readings>) {
        setupFireChart(response)
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = false
        }
    }
}