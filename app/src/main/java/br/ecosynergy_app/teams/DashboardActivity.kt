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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
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
import com.facebook.shimmer.ShimmerFrameLayout
import com.github.mikephil.charting.charts.BarChart
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
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Calendar
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
    private lateinit var txtRefresh: TextView

    private var teamId: Int = 0
    private var teamHandle: String = ""
    private var teamInitial: Int = 0
    private var teamName: String = ""
    private var accessToken: String = ""

    private var refresh = false

    private lateinit var dailyProgressBar: ProgressBar
    private lateinit var txtPercentage: TextView
    private lateinit var imgPercentage: ImageView

    private lateinit var txtValue: TextView
    private lateinit var txtGoal: TextView
    private lateinit var txtMeasure: TextView

    private lateinit var txtDate: TextView

    private lateinit var shimmerTypes: ShimmerFrameLayout
    private lateinit var typesChart: PieChart

    private lateinit var shimmerBars: ShimmerFrameLayout
    private lateinit var barChart: BarChart

    private lateinit var mq7Chart: LineChart
    private lateinit var shimmerMQ7: ShimmerFrameLayout

    private lateinit var fireChart: LineChart
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
        txtRefresh = findViewById(R.id.txtRefresh)

        txtDate = findViewById(R.id.txtDate)
        txtGoal = findViewById(R.id.txtGoal)
        txtValue = findViewById(R.id.txtValue)
        txtMeasure = findViewById(R.id.txtMeasure)

        shimmerBars = findViewById(R.id.shimmerBars)
        barChart = findViewById(R.id.barChart)

        shimmerTypes = findViewById(R.id.shimmerTypes)
        typesChart = findViewById(R.id.typesChart)

        shimmerMQ7 = findViewById(R.id.shimmerMQ7)
        mq7Chart = findViewById(R.id.mq7Chart)

        shimmerMQ7 = findViewById(R.id.shimmerMQ7)
        mq7Chart = findViewById(R.id.mq7Chart)

        shimmerFire = findViewById(R.id.shimmerFire)
        fireChart = findViewById(R.id.fireChart)

        teamId = intent.getIntExtra("TEAM_ID", 0)
        teamInitial = intent.getIntExtra("TEAM_INITIAL", 0)
        teamHandle = intent.getStringExtra("TEAM_HANDLE").toString()
        accessToken = intent.getStringExtra("ACCESS_TOKEN").toString()
        teamName = intent.getStringExtra("TEAM_NAME").toString()

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val todayDate = "$day/${month + 1}/$year"
        txtDate.text = todayDate

        imgTeam.setImageResource(teamInitial)
        lblTeamName.text = teamName
        lblTeamHandle.text = teamHandle

        btnClose.setOnClickListener { finish() }


        btnRefresh.setOnClickListener {
            if (!refresh) {
                imgRefresh.visibility = View.GONE
                progressRefresh.visibility = View.VISIBLE
                txtRefresh.text = "Atualizando"
                refresh = true
            } else {
                imgRefresh.visibility = View.VISIBLE
                progressRefresh.visibility = View.GONE
                txtRefresh.text = "Atualizar"
                refresh = false
            }
        }

        txtDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                txtDate.text = selectedDate
            }, year, month, day)

            datePickerDialog.show()
        }

        //fetchMQ7ReadingsByTeamHandle()
        //fetchFireReadingsByTeamHandle()


        observeTeamInfo()
        observeMQ7ReadingsByTeamHandle()
        setupTypesChart()
    }

    override fun onResume() {
        super.onResume()
        readingsViewModel.getReadingsFromDB(teamHandle)
        teamsViewModel.getTeamById(teamId)
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

    private fun observeFireReadingsByTeamHandle() {
        readingsViewModel.fireReadingsByTeamHandle.observe(this) { response ->
            setupFireChart(response)
            Log.d("DashboardActivity", "FireSensors OK")
        }
    }

    private fun setupMQ7Chart(mq7Readings: List<MQ7Reading>) {
        mq7Chart.visibility = View.GONE
        shimmerMQ7.visibility = View.VISIBLE

        //Log.d("DashboardActivity", "Readings: $mq7Readings")

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


    private fun setupFireChart(fireReadings: List<FireReading>) {
        fireChart.visibility = View.GONE

        shimmerFire.visibility = View.VISIBLE

        Log.d("DashboardActivity", "Readings: $fireReadings")

        val dateFormatIn = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val dateFormatOut = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val aggregatedData = mutableMapOf<String, Int>()

        fireReadings.forEach { reading ->
            val parsedDate = dateFormatIn.parse(reading.timestamp)
            val date = if (parsedDate != null) dateFormatOut.format(parsedDate) else "Invalid Date"
            aggregatedData[date] = aggregatedData.getOrDefault(date, 0) + 1
        }

        Log.d("DashboardActivity", "Aggregated Data: $aggregatedData")

        val sortedData = aggregatedData.entries.sortedBy { it.key }

        val entries = ArrayList<Entry>()
        sortedData.forEachIndexed { index, entry ->
            entries.add(Entry(index.toFloat(), entry.value.toFloat()))
        }

        Log.d("DashboardActivity", "Entries: $entries")

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


    private fun getThemeColor(attrResId: Int): Int {
        val typedValue = TypedValue()
        val theme = this.theme
        theme.resolveAttribute(attrResId, typedValue, true)
        return typedValue.data
    }

    private fun observeTeamInfo() {
        teamsViewModel.teamDB.observe(this) { teamInfo ->
            // Fetch today's aggregated readings
            readingsViewModel.getAggregatedReadingsForToday(teamHandle) {
                // This block will be executed once the data is fetched
                readingsViewModel.aggregatedReadingsForToday.observe(this) { readings ->
                    // Calculate the sum of mq7 and mq135 values
                    val totalEmissions = readings.values.sum()

                    // Update the UI with total emissions
                    txtValue.text = String.format("%.0f", totalEmissions) + " /"
                    txtMeasure.text = " toneladas"
                    txtGoal.text = teamInfo.dailyGoal.toInt().toString()

                    // Remaining code to calculate and set the progress
                    if (teamInfo.dailyGoal != 0.0) {
                        val percentage = (totalEmissions / teamInfo.dailyGoal) * 100
                        txtPercentage.text = String.format("%.2f%%", percentage)
                        dailyProgressBar.progress = percentage.toInt()
                        imgPercentage.visibility = View.VISIBLE

                        // Set color based on percentage
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
}