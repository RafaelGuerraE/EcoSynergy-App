package br.ecosynergy_app.home.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import br.ecosynergy_app.R

class DashboardsAdapter(
    private var dashboardList: List<DashboardItem>,
    private val activity: FragmentActivity,
    private val onItemClick: (DashboardItem) -> Unit
) : RecyclerView.Adapter<DashboardsAdapter.ViewHolder>() {

    // Update the list of dashboard items
    fun updateList(newList: List<DashboardItem>) {
        dashboardList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.dashboards_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dashboardList[position])
    }

    override fun getItemCount(): Int = dashboardList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtName: TextView = itemView.findViewById(R.id.txtName)
        private val txtHandle: TextView = itemView.findViewById(R.id.txtHandle)
        private val imgTeam: ImageView = itemView.findViewById(R.id.imgTeam)
        private val linearClick: LinearLayout = itemView.findViewById(R.id.linearClick)

        fun bind(item: DashboardItem) {
            txtName.text = item.name
            txtHandle.text = item.handle
            imgTeam.setImageResource(item.imageResourceId)

            linearClick.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}

data class DashboardItem(
    val id: Int,
    val name: String,
    val handle: String,
    val imageResourceId: Int
)
