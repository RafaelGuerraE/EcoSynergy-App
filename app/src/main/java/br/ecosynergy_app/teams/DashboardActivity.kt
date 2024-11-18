package br.ecosynergy_app.teams

import android.app.DatePickerDialog
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.readings.ReadingsViewModel
import br.ecosynergy_app.readings.ReadingsViewModelFactory
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.invites.InvitesRepository
import br.ecosynergy_app.room.readings.FireReading
import br.ecosynergy_app.room.readings.MQ7Reading
import br.ecosynergy_app.room.readings.ReadingsRepository
import br.ecosynergy_app.room.teams.MembersRepository
import br.ecosynergy_app.room.teams.TeamsRepository
import br.ecosynergy_app.room.user.UserRepository
import br.ecosynergy_app.teams.viewmodel.TeamsViewModel
import br.ecosynergy_app.teams.viewmodel.TeamsViewModelFactory
import br.ecosynergy_app.user.viewmodel.UserViewModel
import br.ecosynergy_app.user.viewmodel.UserViewModelFactory
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var readingsViewModel: ReadingsViewModel
    private lateinit var teamsViewModel: TeamsViewModel

    private lateinit var btnClose: ImageButton
    private lateinit var lblTeamName: TextView
    private lateinit var lblTeamHandle: TextView
    private lateinit var imgTeam: CircleImageView

    private lateinit var btnRefresh: LinearLayout
    private lateinit var progressRefresh: ProgressBar
    private lateinit var imgRefresh: ImageView

    private var teamId: Int = 0
    private var teamHandle: String = ""
    private var teamInitial: Int = 0
    private var teamName: String = ""
    private var accessToken: String = ""

    private var listTeamHandles: List<String> = listOf()

    private var refresh = false

    private lateinit var dailyProgressBar: ProgressBar
    private lateinit var txtPercentage: TextView
    private lateinit var imgPercentage: ImageView

    private lateinit var txtValue: TextView
    private lateinit var txtGoal: TextView
    private lateinit var txtMeasure: TextView

    private lateinit var linearDailyGoal: LinearLayout

    private lateinit var txtDate: TextView
    private lateinit var shimmerDaily: ShimmerFrameLayout

    private lateinit var shimmerTypes: ShimmerFrameLayout
    private lateinit var typesChart: PieChart

    private lateinit var shimmerReadings: ShimmerFrameLayout
    private lateinit var readingsChart: BarChart

    private lateinit var mq7Chart: LineChart
    private lateinit var shimmerMQ7: ShimmerFrameLayout

    private lateinit var fireChart: BarChart
    private lateinit var shimmerFire: ShimmerFrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val userRepository = UserRepository(AppDatabase.getDatabase(applicationContext).userDao())
        val teamsRepository = TeamsRepository(AppDatabase.getDatabase(applicationContext).teamsDao())
        val invitesRepository = InvitesRepository(AppDatabase.getDatabase(applicationContext).invitesDao())

        val readingsRepository = ReadingsRepository(
            AppDatabase.getDatabase(applicationContext).mq7ReadingsDao(),
            AppDatabase.getDatabase(applicationContext).mq135ReadingsDao(),
            AppDatabase.getDatabase(applicationContext).fireReadingsDao()
        )

        val membersDao = AppDatabase.getDatabase(applicationContext).membersDao()
        val membersRepository = MembersRepository(membersDao)

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(RetrofitClient.userService, userRepository)
        )[UserViewModel::class.java]
        readingsViewModel = ViewModelProvider(
            this,
            ReadingsViewModelFactory(RetrofitClient.readingsService, readingsRepository)
        )[ReadingsViewModel::class.java]
        teamsViewModel = ViewModelProvider(
            this,
            TeamsViewModelFactory(RetrofitClient.teamsService, teamsRepository, RetrofitClient.invitesService, membersRepository, invitesRepository)
        )[TeamsViewModel::class.java]

        teamId = intent.getIntExtra("TEAM_ID", 0)
        teamInitial = intent.getIntExtra("TEAM_INITIAL", 0)
        teamHandle = intent.getStringExtra("TEAM_HANDLE").toString()
        accessToken = intent.getStringExtra("ACCESS_TOKEN").toString()
        teamName = intent.getStringExtra("TEAM_NAME").toString()

        btnClose = findViewById(R.id.btnClose)
        lblTeamName = findViewById(R.id.lblTeamName)
        lblTeamHandle = findViewById(R.id.lblTeamHandle)
        imgTeam = findViewById(R.id.imgTeam)

        txtPercentage = findViewById(R.id.txtPercentage)
        imgPercentage = findViewById(R.id.imgPercentage)
        dailyProgressBar = findViewById(R.id.dailyProgressBar)

        btnRefresh = findViewById(R.id.btnRefresh)
        progressRefresh = findViewById(R.id.progressRefresh)
        imgRefresh = findViewById(R.id.imgRefresh)

        txtDate = findViewById(R.id.txtDate)
        txtGoal = findViewById(R.id.txtGoal)
        txtValue = findViewById(R.id.txtValue)
        txtMeasure = findViewById(R.id.txtMeasure)

        shimmerReadings = findViewById(R.id.shimmerReadings)
        readingsChart = findViewById(R.id.readingsChart)

        shimmerTypes = findViewById(R.id.shimmerTypes)
        typesChart = findViewById(R.id.typesChart)

        shimmerMQ7 = findViewById(R.id.shimmerMQ7)
        mq7Chart = findViewById(R.id.mq7Chart)

        shimmerMQ7 = findViewById(R.id.shimmerMQ7)
        mq7Chart = findViewById(R.id.mq7Chart)

        linearDailyGoal = findViewById(R.id.linearDailyGoal)
        shimmerDaily = findViewById(R.id.shimmerDaily)

        shimmerFire = findViewById(R.id.shimmerFire)
        fireChart = findViewById(R.id.fireChart)

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val todayDate = "$day/${month + 1}/$year"
        txtDate.text = todayDate

        imgTeam.setImageResource(teamInitial)
        lblTeamName.text = teamName
        lblTeamHandle.text = teamHandle

        lifecycleScope.launch {
            teamsViewModel.getAllTeamsFromDB().collectLatest { teamData ->
                listTeamHandles = teamData.map { it.handle }
                Log.d("DashboardActivity", "$listTeamHandles")
            }
        }

        btnClose.setOnClickListener { finish() }

        btnRefresh.setOnClickListener {
            if (!refresh) {
                imgRefresh.visibility = View.GONE
                progressRefresh.visibility = View.VISIBLE
                btnRefresh.isFocusable = false
                refresh = true

                readingsViewModel.fetchAllReadings(listTeamHandles, accessToken){
                    refresh = false
                    progressRefresh.visibility = View.GONE
                    imgRefresh.visibility = View.VISIBLE
                    btnRefresh.isFocusable = true

                    readingsViewModel.getReadingsFromDB(teamHandle)
                    readingsViewModel.getFireReadingsByHour(teamHandle)
                    readingsViewModel.getMQ135ReadingsByHour(teamHandle)
                }
            } else {
                showToast("Atualizando...")
            }
        }

        txtDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                shimmerDaily.animate().alpha(1f).setDuration(300).withEndAction {
                    shimmerDaily.visibility = View.VISIBLE
                    linearDailyGoal.visibility = View.GONE
                    linearDailyGoal.animate().alpha(0f).setDuration(300)
                }

                val calendar = Calendar.getInstance()
                calendar.set(selectedYear, selectedMonth, selectedDay)
                val selectedDate = calendar.time

                txtDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate)

                readingsViewModel.getAggregatedReadingsForDate(teamHandle, selectedDate) {
                    observeGoalInfo(selectedDate){
                        shimmerDaily.animate().alpha(0f).setDuration(300).withEndAction {
                            shimmerDaily.visibility = View.GONE
                            linearDailyGoal.visibility = View.VISIBLE
                            linearDailyGoal.animate().alpha(1f).setDuration(300)
                        }
                    }
                }
            }, year, month, day)

            datePickerDialog.show()
        }

        val selectedDate = calendar.time
        observeGoalInfo(selectedDate){
            shimmerDaily.animate().alpha(0f).setDuration(300).withEndAction {
                shimmerDaily.visibility = View.GONE
                linearDailyGoal.visibility = View.VISIBLE
                linearDailyGoal.animate().alpha(1f).setDuration(300)
            }
        }
        observeMQ7ReadingsByTeamHandle()
        observeFireReadingsByHour()
        observeReadingsByHour()
        setupTypesChart()
    }

    override fun onResume() {
        super.onResume()
        teamsViewModel.getTeamByIdFromDB(teamId)
        readingsViewModel.getReadingsFromDB(teamHandle)
        readingsViewModel.getFireReadingsByHour(teamHandle)
        readingsViewModel.getMQ135ReadingsByHour(teamHandle)
    }

    private fun setupTypesChart() {
        typesChart.visibility = View.GONE
        shimmerTypes.visibility = View.VISIBLE

        typesChart.setUsePercentValues(true)
        typesChart.description.isEnabled = false
        typesChart.isDrawHoleEnabled = false
        typesChart.setEntryLabelColor(android.R.color.white)

        typesChart.setEntryLabelTextSize(14f)
        typesChart.legend.textColor = getThemeColor(android.R.attr.textColorPrimary)

        typesChart.animateY(1000, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)

        val pieEntries = mutableListOf<PieEntry>()
        val label = ""

        val statsMap = mapOf("Propano(C3H8)" to 92.5f, "Dióxido de Carbono(CO2)" to 5.3f, "Outros" to 1.2f)

        statsMap.forEach { (key, value) ->
            pieEntries.add(PieEntry(value, key))
        }

        val pieDataSet = PieDataSet(pieEntries, label)

        pieDataSet.valueTextColor = getThemeColor(android.R.attr.textColorPrimary)

        pieDataSet.colors = listOf(
            ContextCompat.getColor(this, R.color.blue),
            ContextCompat.getColor(this, R.color.greenDark),
            ContextCompat.getColor(this, R.color.green)
        )

        pieDataSet.sliceSpace = 2f
        pieDataSet.selectionShift = 5f

        val pieData = PieData(pieDataSet)
        pieData.setValueTextSize(12f)
        pieData.setValueTextColor(ContextCompat.getColor(this, android.R.color.white))

        pieData.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${value.toInt()}%"
            }
        })

        typesChart.data = pieData

        typesChart.invalidate()
        shimmerTypes.animate().alpha(0f).setDuration(300).withEndAction {
            shimmerTypes.stopShimmer()
            shimmerTypes.visibility = View.GONE
            typesChart.visibility = View.VISIBLE
            typesChart.animate().alpha(1f).setDuration(300)
        }
    }

    private fun observeMQ7ReadingsByTeamHandle() {
        readingsViewModel.mq7ReadingsByTeamHandle.observe(this) { response ->
            setupMQ7Chart(response)
            Log.d("DashboardActivity", "Sensors MQ7 OK")
        }
    }

    private fun setupMQ7Chart(mq7Readings: List<MQ7Reading>) {
        mq7Chart.visibility = View.GONE
        shimmerMQ7.visibility = View.VISIBLE

        val dateFormatIn = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val dateFormatOut = SimpleDateFormat("MM/dd", Locale.getDefault())
        val aggregatedData = mutableMapOf<String, Int>()

        mq7Readings.forEach { reading ->
            val parsedDate = dateFormatIn.parse(reading.timestamp)
            val date = if (parsedDate != null) dateFormatOut.format(parsedDate) else "Invalid Date"
            aggregatedData[date] = aggregatedData.getOrDefault(date, 0) + 1
        }

//        Log.d("DashboardActivity", "MQ7 Aggregated Data: $aggregatedData")

        val sortedData = aggregatedData.entries.sortedBy { it.key }

        val entries = ArrayList<Entry>()
        sortedData.forEachIndexed { index, entry ->
            entries.add(Entry(index.toFloat(), entry.value.toFloat()))
        }

        Log.d("DashboardActivity", "Entries: $entries")

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

    private fun setupFireBarChart(hourlyData: Map<Int, Int>) {
        shimmerFire.animate().alpha(1f).setDuration(300).withEndAction {
            shimmerFire.visibility = View.VISIBLE
            fireChart.visibility = View.GONE
            fireChart.animate().alpha(0f).setDuration(300)
        }

        val entries = ArrayList<BarEntry>()
        for (hour in 0..23) {
            val count = hourlyData[hour] ?: 0
            entries.add(BarEntry(hour.toFloat(), count.toFloat()))
        }

        val dataSet = BarDataSet(entries, "Alertas por Hora").apply {
            color = ContextCompat.getColor(this@DashboardActivity, R.color.red)
            valueTextColor = getThemeColor(android.R.attr.textColorPrimary)
            valueTextSize = 10f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }

        fireChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter((0..23).map { it.toString() })
            setDrawGridLines(false)
            textColor = getThemeColor(android.R.attr.textColorPrimary)
            granularity = 1f
        }

        fireChart.axisLeft.apply {
            setDrawGridLines(false)
            textColor = getThemeColor(android.R.attr.textColorPrimary)
            axisMinimum = 0f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }

        fireChart.axisRight.isEnabled = false

        fireChart.apply {
            description.isEnabled = false
            legend.textColor = getThemeColor(android.R.attr.textColorPrimary)
            isDragEnabled = true
            isScaleXEnabled = true
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.9f
        fireChart.data = barData
        fireChart.invalidate()

        shimmerFire.animate().alpha(0f).setDuration(300).withEndAction {
            shimmerFire.visibility = View.GONE
            fireChart.visibility = View.VISIBLE
            fireChart.animate().alpha(1f).setDuration(300)
        }
    }

    private fun observeFireReadingsByHour() {
        readingsViewModel.fireReadingsByHour.observe(this) { hourlyData ->
            setupFireBarChart(hourlyData)
            Log.d("DashboardActivity", "Fire readings by hour observed")
        }
    }

    private fun setupReadingsBarChart(hourlyData: Map<Int, Int>) {
        shimmerReadings.animate().alpha(1f).setDuration(300).withEndAction {
            shimmerReadings.visibility = View.VISIBLE
            readingsChart.visibility = View.GONE
            readingsChart.animate().alpha(0f).setDuration(300)
        }

        val entries = ArrayList<BarEntry>()
        for (hour in 0..23) {
            val count = hourlyData[hour] ?: 0
            entries.add(BarEntry(hour.toFloat(), count.toFloat()))
        }

        val dataSet = BarDataSet(entries, "Leituras por Hora").apply {
            color = ContextCompat.getColor(this@DashboardActivity, R.color.blue)
            valueTextColor = getThemeColor(android.R.attr.textColorPrimary)
            valueTextSize = 10f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }

        readingsChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter((0..23).map { it.toString() })
            setDrawGridLines(false)
            textColor = getThemeColor(android.R.attr.textColorPrimary)
            granularity = 1f
        }

        readingsChart.axisLeft.apply {
            setDrawGridLines(false)
            textColor = getThemeColor(android.R.attr.textColorPrimary)
            axisMinimum = 0f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }

        readingsChart.axisRight.isEnabled = false

        readingsChart.apply {
            description.isEnabled = false
            legend.textColor = getThemeColor(android.R.attr.textColorPrimary)
            isDragEnabled = true
            isScaleXEnabled = true
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.8f
        readingsChart.data = barData
        readingsChart.invalidate()

        shimmerReadings.animate().alpha(0f).setDuration(300).withEndAction {
            shimmerReadings.visibility = View.GONE
            readingsChart.visibility = View.VISIBLE
            readingsChart.animate().alpha(1f).setDuration(300)
        }
    }

    private fun observeReadingsByHour() {
        readingsViewModel.mq135ReadingsByHour.observe(this) { hourlyData ->
            setupReadingsBarChart(hourlyData)
            Log.d("DashboardActivity", "MQ135 readings by hour observed")
        }
    }

    private fun getThemeColor(attrResId: Int): Int {
        val typedValue = TypedValue()
        val theme = this.theme
        theme.resolveAttribute(attrResId, typedValue, true)
        return typedValue.data
    }

    private fun observeGoalInfo(date: Date, onComplete: () -> Unit) {
        teamsViewModel.teamDB.observe(this) { teamInfo ->

            readingsViewModel.getAggregatedReadingsForDate(teamHandle, date) {
                readingsViewModel.aggregatedReadingsForDate.observe(this) { readings ->
                    val totalEmissions = readings.values.sum()

                    txtValue.text = formatGoal(totalEmissions.toDouble()) + " /"
                    txtMeasure.text = " toneladas"
                    txtGoal.text = formatGoal(teamInfo.dailyGoal)

                    if (teamInfo.dailyGoal != 0.0) {
                        val percentage = (totalEmissions / teamInfo.dailyGoal) * 100
                        txtPercentage.text = String.format("%.2f%%", percentage)
                        dailyProgressBar.progress = percentage.toInt()
                        imgPercentage.visibility = View.VISIBLE

                        when {
                            percentage < 60.00 -> {
                                imgPercentage.setImageResource(R.drawable.ic_decrease)
                                imgPercentage.setColorFilter(ContextCompat.getColor(this, R.color.green), PorterDuff.Mode.SRC_IN)
                                dailyProgressBar.progressTintList = ContextCompat.getColorStateList(this, R.color.green)
                            }
                            percentage < 100.00 -> {
                                imgPercentage.setImageResource(R.drawable.ic_decrease)
                                imgPercentage.setColorFilter(ContextCompat.getColor(this, R.color.orange), PorterDuff.Mode.SRC_IN)
                                dailyProgressBar.progressTintList = ContextCompat.getColorStateList(this, R.color.orange)
                            }
                            percentage >= 100.00 -> {
                                imgPercentage.setImageResource(R.drawable.ic_rise)
                                imgPercentage.setColorFilter(ContextCompat.getColor(this, R.color.red), PorterDuff.Mode.SRC_IN)
                                dailyProgressBar.progressTintList = ContextCompat.getColorStateList(this, R.color.red)
                            }
                        }
                    } else {
                        txtPercentage.text = "N/A"
                        dailyProgressBar.progress = 0
                        imgPercentage.visibility = View.GONE
                    }

                    onComplete()
                }
            }
        }
    }

    private fun formatGoal(goal: Double): String {
        return when {
            goal < 1000 -> goal.toInt().toString()
            goal < 1_000_000 -> "${(goal / 1000).toInt()} mil"
            goal < 1_000_000_000 -> "${(goal / 1_000_000).toInt()} milhões"
            else -> "${(goal / 1_000_000_000).toInt()} bilhões"
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}