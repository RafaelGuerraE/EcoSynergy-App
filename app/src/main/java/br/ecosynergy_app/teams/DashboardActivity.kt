package br.ecosynergy_app.teams

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.readings.ReadingsViewModel
import br.ecosynergy_app.readings.ReadingsViewModelFactory
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.readings.FireReading
import br.ecosynergy_app.room.readings.MQ135Reading
import br.ecosynergy_app.room.readings.MQ7Reading
import br.ecosynergy_app.room.readings.ReadingsRepository
import br.ecosynergy_app.room.teams.MembersRepository
import br.ecosynergy_app.room.teams.TeamsRepository
import br.ecosynergy_app.room.user.UserRepository
import br.ecosynergy_app.teams.viewmodel.TeamsViewModel
import br.ecosynergy_app.teams.viewmodel.TeamsViewModelFactory
import br.ecosynergy_app.user.viewmodel.UserViewModel
import br.ecosynergy_app.user.viewmodel.UserViewModelFactory
import com.facebook.shimmer.ShimmerFrameLayout
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Locale

class DashboardActivity : AppCompatActivity() {


    private lateinit var userViewModel: UserViewModel
    private lateinit var readingsViewModel: ReadingsViewModel
    private lateinit var teamsViewModel: TeamsViewModel

    private lateinit var btnClose : ImageButton
    private lateinit var lblTeamName: TextView
    private lateinit var lblTeamHandle: TextView
    private lateinit var imgTeam: CircleImageView

    private lateinit var btnRefresh: LinearLayout
    private lateinit var progressRefresh: ProgressBar
    private lateinit var imgRefresh: ImageView
    private lateinit var txtRefresh: TextView

    private var teamHandle: String? = null
    private var teamInitial: Int = 0
    private var teamName: String = ""
    private var accessToken: String =""

    private var refresh = false

    private lateinit var mq7Chart: LineChart
    private lateinit var shimmerMQ7: ShimmerFrameLayout

    private lateinit var mq135Chart: LineChart
    private lateinit var shimmerMQ135: ShimmerFrameLayout

    private lateinit var fireChart: LineChart
    private lateinit var shimmerFire: ShimmerFrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val userDao = AppDatabase.getDatabase(applicationContext).userDao()
        val userRepository = UserRepository(userDao)

        val teamsDao = AppDatabase.getDatabase(applicationContext).teamsDao()
        val teamsRepository = TeamsRepository(teamsDao)

        val readingsRepository = ReadingsRepository(AppDatabase.getDatabase(applicationContext).mq7ReadingsDao(), AppDatabase.getDatabase(applicationContext).mq135ReadingsDao(), AppDatabase.getDatabase(applicationContext).fireReadingsDao())

        val membersDao = AppDatabase.getDatabase(applicationContext).membersDao()
        val membersRepository = MembersRepository(membersDao)

        userViewModel = ViewModelProvider(this, UserViewModelFactory(RetrofitClient.userService, userRepository))[UserViewModel::class.java]
        readingsViewModel = ViewModelProvider(this, ReadingsViewModelFactory(RetrofitClient.readingsService, readingsRepository))[ReadingsViewModel::class.java]
        teamsViewModel = ViewModelProvider(this, TeamsViewModelFactory(RetrofitClient.teamsService, teamsRepository, membersRepository))[TeamsViewModel::class.java]


        btnClose = findViewById(R.id.btnClose)
        lblTeamName = findViewById(R.id.lblTeamName)
        lblTeamHandle = findViewById(R.id.lblTeamHandle)
        imgTeam = findViewById(R.id.imgTeam)

        btnRefresh = findViewById(R.id.btnRefresh)
        progressRefresh = findViewById(R.id.progressRefresh)
        imgRefresh = findViewById(R.id.imgRefresh)
        txtRefresh = findViewById(R.id.txtRefresh)

        shimmerMQ7 = findViewById(R.id.shimmerMQ7)
        mq7Chart = findViewById(R.id.mq7Chart)

        shimmerMQ135 = findViewById(R.id.shimmerMQ135)
        mq135Chart = findViewById(R.id.mq135Chart)

        shimmerFire = findViewById(R.id.shimmerFire)
        fireChart = findViewById(R.id.fireChart)

        teamInitial = intent.getIntExtra("TEAM_INITIAL", 0)
        teamHandle = intent.getStringExtra("TEAM_HANDLE").toString()
        accessToken = intent.getStringExtra("ACCESS_TOKEN").toString()
        teamName = intent.getStringExtra("TEAM_NAME").toString()

        imgTeam.setImageResource(teamInitial)
        lblTeamName.text = teamName
        lblTeamHandle.text = teamHandle

        btnClose.setOnClickListener{ finish() }


        btnRefresh.setOnClickListener{
            if (!refresh) {
                imgRefresh.visibility = View.GONE
                progressRefresh.visibility = View.VISIBLE
                txtRefresh.text = "Atualizando"
                refresh = true
            }
            else{
                imgRefresh.visibility = View.VISIBLE
                progressRefresh.visibility = View.GONE
                txtRefresh.text = "Atualizar dados"
                refresh = false
            }
        }

        //fetchMQ7ReadingsByTeamHandle()
        //fetchMQ135ReadingsByTeamHandle()
        //fetchFireReadingsByTeamHandle()
    }

    private fun fetchMQ7ReadingsByTeamHandle() {
//        readingsViewModel.mq7ReadingsDB.removeObservers(this)
//        readingsViewModel.getReadingsBySensorFromDB("MQ7")
//        readingsViewModel.mq7ReadingsDB.observe(this) { response ->
//            handleMQ7Readings(response)
//            Log.d("HomeFragment", "Sensors MQ7 OK")
//        }
    }

    private fun fetchMQ135ReadingsByTeamHandle() {
//        readingsViewModel.mq135ReadingsDB.removeObservers(this)
//        readingsViewModel.getReadingsBySensorFromDB("MQ135")
//        readingsViewModel.mq135ReadingsDB.observe(this) { response ->
//            handleMQ135Readings(response)
//            Log.d("HomeFragment", "Sensors MQ135 OK")
//        }
    }

    private fun fetchFireReadingsByTeamHandle() {
//        readingsViewModel.mq7ReadingsDB.removeObservers(this)
//        readingsViewModel.getReadingsBySensorFromDB("FIRE")
//        readingsViewModel.fireReadingsDB.observe(this) { response ->
//            handleFireReadings(response)
//            Log.d("HomeFragment", "FireSensors OK")
//        }
    }

    private fun setupMQ7Chart(mq7Readings: List<MQ7Reading>) {
        mq7Chart.visibility = View.GONE
        shimmerMQ7.visibility = View.VISIBLE

        //Log.d("HomeFragment", "Readings: $mq7Readings")

        val dateFormatIn = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val dateFormatOut = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val aggregatedData = mutableMapOf<String, Int>()

        mq7Readings.forEach { reading ->
            val parsedDate = dateFormatIn.parse(reading.timestamp)
            val date = if (parsedDate != null) dateFormatOut.format(parsedDate) else "Invalid Date"
            aggregatedData[date] = aggregatedData.getOrDefault(date, 0) + 1
        }

        Log.d("HomeFragment", "MQ7 Aggregated Data: $aggregatedData")

        val sortedData = aggregatedData.entries.sortedBy { it.key }

        val entries = ArrayList<Entry>()
        sortedData.forEachIndexed { index, entry ->
            entries.add(Entry(index.toFloat(), entry.value.toFloat()))
        }

        Log.d("HomeFragment", "Entries: $entries")

        val dataSet = LineDataSet(entries, "Nº de alertas por dia").apply {
            color = ContextCompat.getColor(this@DashboardActivity, R.color.greenDark)
            valueTextColor = getThemeColor(android.R.attr.textColorPrimary)
            valueTextSize = 10f
            setDrawValues(true)
            lineWidth = 3f
            setDrawCircles(true)
            setCircleColor(ContextCompat.getColor(this@DashboardActivity, R.color.greenDark))
            circleRadius = 5f
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(this@DashboardActivity, R.color.greenDark_50)
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

    private fun setupMQ135Chart(mq135Readings: List<MQ135Reading>) {
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
            color = ContextCompat.getColor(this@DashboardActivity, R.color.yellow)
            valueTextColor = getThemeColor(android.R.attr.textColorPrimary)
            valueTextSize = 10f
            setDrawValues(true)
            lineWidth = 3f
            setDrawCircles(true)
            setCircleColor(ContextCompat.getColor(this@DashboardActivity, R.color.yellow))
            circleRadius = 5f
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(this@DashboardActivity, R.color.yellow)
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


    private fun setupFireChart(fireReadings: List<FireReading>) {
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
            color = ContextCompat.getColor(this@DashboardActivity, R.color.red)
            valueTextColor = getThemeColor(android.R.attr.textColorPrimary)
            valueTextSize = 10f
            setDrawValues(true)
            lineWidth = 3f
            setDrawCircles(true)
            setCircleColor(ContextCompat.getColor(this@DashboardActivity, R.color.red))
            circleRadius = 5f
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(this@DashboardActivity, R.color.red)
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

    private fun handleMQ7Readings(response: List<MQ7Reading>) {
        setupMQ7Chart(response)
    }

    private fun handleMQ135Readings(response: List<MQ135Reading>) {
        setupMQ135Chart(response)
    }

    private fun handleFireReadings(response: List<FireReading>) {
        setupFireChart(response)
    }


    private fun getThemeColor(attrResId: Int): Int {
        val typedValue = TypedValue()
        val theme = this.theme
        theme.resolveAttribute(attrResId, typedValue, true)
        return typedValue.data
    }
}