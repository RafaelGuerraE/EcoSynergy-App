package br.ecosynergy_app.home.homefragments

import android.content.Context
import android.content.SharedPreferences
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.home.UserViewModel
import br.ecosynergy_app.home.UserViewModelFactory
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.core.ui.ChartCredits
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout

class Home : Fragment() {

    private lateinit var lblFirstname: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var shimmerViewContainer: ShimmerFrameLayout
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val chart = view.findViewById<AnyChartView>(R.id.chart)
        val shimmerLayout: ShimmerFrameLayout = view.findViewById(R.id.shimmerLayout)

        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        lblFirstname = view.findViewById(R.id.lblFirstname)
        shimmerViewContainer = view.findViewById(R.id.shimmerViewContainer)

        chart.setProgressBar(shimmerLayout)
        chart.setBackgroundColor(getThemeColor(requireContext(),android.R.attr.colorBackground))
        setupChart(chart)
        observeUserData()

        refreshApp()

        return view
    }

    private fun setupChart(anyChartView: AnyChartView) {
        val pie = AnyChart.pie()
        val data: MutableList<DataEntry> = ArrayList()

        val context = requireContext()
        data.add(ValueDataEntry("USA", 6371664))
        data.add(ValueDataEntry("China", 7215872))
        data.add(ValueDataEntry("Japan", 1487196))
        data.add(ValueDataEntry("Germany", 1200000))
        data.add(ValueDataEntry("UK", 720000))

        val colors = arrayOf(
            getColorHexString(context, R.color.green),
            getColorHexString(context, R.color.greenDark),
            getColorHexString(context, R.color.blue),
            getColorHexString(context, R.color.yellow),
            getColorHexString(context, R.color.blackBg)
        )

        pie.palette(colors)
        pie.data(data)

        pie.background().fill(getThemeColor(context, android.R.attr.colorBackground))
        pie.labels().fontColor(getColorHexString(context, R.color.white))
        pie.legend().fontColor(getThemeColor(context, android.R.attr.textColorPrimary))


        val credits: ChartCredits = pie.credits()
        credits.enabled(false)
        anyChartView.setChart(pie)
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

    private fun getColorHexString(context: Context, colorResId: Int): String {
        val colorInt = ContextCompat.getColor(context, colorResId)
        return String.format("#%06X", (0xFFFFFF and colorInt))
    }

    private fun getThemeColor(context: Context, attrResId: Int): String {
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(attrResId, typedValue, true)
        val colorInt = typedValue.data
        return String.format("#%06X", (0xFFFFFF and colorInt))
    }

    private fun refreshApp() {
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = false
        }
    }
}