package br.ecosynergy_app.home.homefragments

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import br.ecosynergy_app.R
import br.ecosynergy_app.sensors.FireReadingsResponse
import br.ecosynergy_app.sensors.MQ135ReadingsResponse
import br.ecosynergy_app.user.UserViewModel
import br.ecosynergy_app.sensors.MQ7ReadingsResponse
import br.ecosynergy_app.sensors.ReadingVO
import br.ecosynergy_app.sensors.SensorsViewModel
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

    private lateinit var mq7Chart: LineChart
    private lateinit var shimmerMQ7: ShimmerFrameLayout

    private lateinit var mq135Chart: LineChart
    private lateinit var shimmerMQ135: ShimmerFrameLayout

    private lateinit var fireChart: LineChart
    private lateinit var shimmerFire: ShimmerFrameLayout

    private val userViewModel: UserViewModel by activityViewModels()

    private val sensorsViewModel: SensorsViewModel by activityViewModels()

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

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeUserData()
        observeMQ7()
        observeMQ135()
        observeFireSensor()
        setupSwipeRefresh()
    }

    private fun observeUserData() {
        userViewModel.user.observe(viewLifecycleOwner) { result ->
            shimmerName.animate().alpha(0f).setDuration(300).withEndAction {
                shimmerName.stopShimmer()
                shimmerName.visibility = View.GONE
                lblFirstname.visibility = View.VISIBLE
                lblFirstname.animate().alpha(1f).setDuration(300)
            }
            result.onSuccess { user ->
                val firstName = user.fullName.split(" ").firstOrNull()
                lblFirstname.text = "$firstName!"
            }.onFailure { throwable ->
                Log.e("HomeFragment", "Error fetching user data", throwable)
                lblFirstname.text = ""
            }
        }
    }

    private fun getThemeColor(attrResId: Int): Int {
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(attrResId, typedValue, true)
        return typedValue.data
    }

    private fun setupMQ7Chart(mq7Readings: List<ReadingVO>) {
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

        Log.d("HomeFragment", "Aggregated Data: $aggregatedData")

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
            valueFormatter = DefaultValueFormatter(0) // Display integers instead of floats
        }

        mq7Chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(sortedData.map { it.key })
            setDrawGridLines(false)
            textColor = getThemeColor(android.R.attr.textColorPrimary)
            setLabelCount(sortedData.size, true)
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

    private fun observeMQ7(){
        sensorsViewModel.mq7ReadingResult.observe(viewLifecycleOwner){ result->
            result.onSuccess { response->
                handleMQ7Readings(response)
                Log.d("TeamsFragment", "Sensors MQ7Response OK")
            }.onFailure { e->
                Log.e("TeamsFragment", "Error MQ7", e)
            }
        }
    }

    private fun observeMQ135(){
        sensorsViewModel.mq135ReadingResult.observe(viewLifecycleOwner){ result->
            result.onSuccess { response->
                handleMQ135Readings(response)
                Log.d("TeamsFragment", "Sensors MQ135Response OK")
            }.onFailure { e->
                Log.e("TeamsFragment", "Error MQ135", e)
            }
        }
    }

    private fun observeFireSensor(){
        sensorsViewModel.fireReadingResult.observe(viewLifecycleOwner){ result->
            result.onSuccess { response->
                handleFireReadings(response)
                Log.d("TeamsFragment", "Sensors FireResponse OK")
            }.onFailure { e->
                Log.e("TeamsFragment", "Fire Error", e)
            }
        }
    }

    private fun setupMQ135Chart(mq135Readings: List<ReadingVO>) {
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
            textColor = getThemeColor(android.R.attr.textColorPrimary)
            setLabelCount(sortedData.size, true)
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
            isScaleXEnabled = true
            setExtraOffsets(10f, 10f, 10f, 10f)
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

    private fun setupFireChart(fireReadings: List<ReadingVO>) {
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

    private fun handleMQ7Readings(response: MQ7ReadingsResponse) {
        val readingsData = response.embedded.mQ7ReadingVOList
        setupMQ7Chart(readingsData)
    }

    private fun handleMQ135Readings(response: MQ135ReadingsResponse) {
        val readingsData = response.embedded.mQ135ReadingVOList
        setupMQ135Chart(readingsData)
    }

    private fun handleFireReadings(response: FireReadingsResponse) {
        val readingsData = response.embedded.fireReadingVOList
        setupFireChart(readingsData)
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = false
        }
    }
}