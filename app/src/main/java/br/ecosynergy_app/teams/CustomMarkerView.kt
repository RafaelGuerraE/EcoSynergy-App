package br.ecosynergy_app.teams

import android.content.Context
import android.widget.TextView
import br.ecosynergy_app.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CustomMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {

    private val tvContent: TextView = findViewById(R.id.tvContent)

    override fun refreshContent(e: Entry, highlight: Highlight) {
        // Convert the x value (timestamp) to a date format
        val timestamp = e.x.toLong()
        val date = convertTimestampToDate(timestamp)

        tvContent.text = "Date: $date"
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2).toFloat(), -height.toFloat())
    }

    private fun convertTimestampToDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = Date(timestamp)
        return sdf.format(date)
    }
}
