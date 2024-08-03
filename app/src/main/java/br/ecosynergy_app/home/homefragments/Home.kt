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
import java.text.SimpleDateFormat
import java.util.Locale

class Home : Fragment() {

    private lateinit var lblFirstname: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var shimmerViewContainer: ShimmerFrameLayout
    private lateinit var mq7LineChart: LineChart

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
        shimmerViewContainer = view.findViewById(R.id.shimmerViewContainer)
        mq7LineChart = view.findViewById(R.id.mq7LineChart)

        sensorsViewModel.mq7ReadingResult.observe(viewLifecycleOwner){ result->
            result.onSuccess { response->
                handleReadingsData(response)
                Log.d("TeamsFragment", "Sensors MQ7Response OK")
            }.onFailure { e->
                Log.e("TeamsFragment", "Error", e)
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeUserData()
        setupSwipeRefresh()
    }

    private fun observeUserData() {
        userViewModel.user.observe(viewLifecycleOwner) { result ->
            shimmerViewContainer.animate().alpha(0f).setDuration(300).withEndAction {
                shimmerViewContainer.stopShimmer()
                shimmerViewContainer.visibility = View.GONE
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

    private fun setupMq7Chart(mq7Readings: List<ReadingVO>) {
        val entries = ArrayList<Entry>()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())

        mq7Readings.forEachIndexed { index, reading ->
            val timestamp = dateFormat.parse(reading.timestamp)?.time?.toFloat()
            if (timestamp != null) {
                entries.add(Entry(timestamp, reading.value.toFloat()))
            } else {
                Log.d("HomeFragment", "Parsing failed for: ${reading.timestamp}")
            }
        }

        val dataSet = LineDataSet(entries, "Readings Values").apply {
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
        }

        mq7LineChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = DateValueFormatter()
            setDrawGridLines(true)
            textColor = getThemeColor(android.R.attr.textColorPrimary)
            setLabelCount(10, true)
        }

        mq7LineChart.axisLeft.apply {
            setDrawGridLines(false)
            textColor = getThemeColor(android.R.attr.textColorPrimary)
        }

        mq7LineChart.axisRight.isEnabled = false
        mq7LineChart.description.isEnabled = false
        mq7LineChart.legend.textColor = getThemeColor(android.R.attr.textColorPrimary)

        Log.d("HomeFragment", "Entries: $entries")

        val lineData = LineData(dataSet)
        mq7LineChart.data = lineData
        mq7LineChart.invalidate()
    }


    private fun handleReadingsData(response: MQ7ReadingsResponse) {
        val readingsData = response.embedded.mQ7ReadingVOList
        setupMq7Chart(readingsData)
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = false
        }
    }
}
