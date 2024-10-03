package br.ecosynergy_app.teams

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import br.ecosynergy_app.R
import br.ecosynergy_app.home.HomeActivity
import br.ecosynergy_app.room.Teams

class TeamAdapter(private var teamsList: List<Teams>) :
    RecyclerView.Adapter<TeamAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.team_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(teamsList[position])
    }

    override fun getItemCount(): Int = teamsList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val teamName: TextView = itemView.findViewById(R.id.txtName)
        private val teamHandle: TextView = itemView.findViewById(R.id.txtHandle)
        private val teamDescription: TextView = itemView.findViewById(R.id.txtDescription)
        private val teamImage: ImageView = itemView.findViewById(R.id.imgTeam)
        private val btnInfo: ImageButton = itemView.findViewById(R.id.btnInfo)
        private val linearClick: LinearLayout = itemView.findViewById(R.id.linearClick)

        fun bind(team: Teams) {
            teamName.text = team.name
            teamHandle.text = "@${team.handle}"
            teamDescription.text = team.description

            val drawableLetter = HomeActivity().getDrawableForLetter(team.name.first())

            teamImage.setImageResource(drawableLetter)

            btnInfo.setOnClickListener {
                val context = itemView.context
                val i = Intent(context, TeamOverviewActivity::class.java)
                i.apply {
                    putExtra("TEAM_ID", team.id)
                    putExtra("TEAM_HANDLE", team.handle)
                }
                context.startActivity(i)
            }

            linearClick.setOnClickListener {
                val fragmentManager = (itemView.context as AppCompatActivity).supportFragmentManager
                val teamBottomSheet = TeamBottomSheet().apply {
                    arguments = Bundle().apply {
                        putString("TEAM_HANDLE", team.handle)
                        putInt("TEAM_INITIAL", drawableLetter)
                    }
                }
                teamBottomSheet.show(fragmentManager, "TeamBottomSheet")
            }
        }
    }
}
