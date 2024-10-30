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
    private val onItemClick: (DashboardItem) -> Unit,
    private val onCreateTeamClick: () -> Unit,   // Callback for "Create Team"
    private val onAllTeamsRedirectClick: () -> Unit // Callback for "View All Teams"
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_TEAM = 1
    private val VIEW_TYPE_CREATE_TEAM = 2
    private val VIEW_TYPE_ALL_TEAMS_REDIRECT = 3

    fun updateList(newList: List<DashboardItem>) {
        dashboardList = newList
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            dashboardList.isEmpty() -> VIEW_TYPE_CREATE_TEAM
            position == dashboardList.size -> VIEW_TYPE_ALL_TEAMS_REDIRECT
            else -> VIEW_TYPE_TEAM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TEAM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.dashboards_layout, parent, false)
                TeamViewHolder(view)
            }
            VIEW_TYPE_CREATE_TEAM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.create_team_layout, parent, false)
                CreateTeamViewHolder(view)
            }
            VIEW_TYPE_ALL_TEAMS_REDIRECT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.all_teams_redirect_layout, parent, false)
                AllTeamsRedirectViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TeamViewHolder -> holder.bind(dashboardList[position])
            is CreateTeamViewHolder -> holder.bind()
            is AllTeamsRedirectViewHolder -> holder.bind()
        }
    }

    override fun getItemCount(): Int {
        return if (dashboardList.isEmpty()) 1 else dashboardList.size + 1
    }

    inner class TeamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtName: TextView = itemView.findViewById(R.id.txtName)
        private val txtHandle: TextView = itemView.findViewById(R.id.txtHandle)
        private val imgTeam: ImageView = itemView.findViewById(R.id.imgTeam)
        private val linearClick: LinearLayout = itemView.findViewById(R.id.linearClick)

        fun bind(item: DashboardItem) {
            txtName.text = item.name
            txtHandle.text = item.handle
            imgTeam.setImageResource(item.imageResourceId)
            linearClick.setOnClickListener { onItemClick(item) }
        }
    }

    inner class CreateTeamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val createTeamButton: View = itemView.findViewById(R.id.linearClick)

        fun bind() {
            createTeamButton.setOnClickListener { onCreateTeamClick() }
        }
    }

    inner class AllTeamsRedirectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val allTeamsButton: View = itemView.findViewById(R.id.linearClick)

        fun bind() {
            allTeamsButton.setOnClickListener { onAllTeamsRedirectClick() }
        }
    }
}


data class DashboardItem(
    val id: Int,
    val name: String,
    val handle: String,
    val imageResourceId: Int
)
