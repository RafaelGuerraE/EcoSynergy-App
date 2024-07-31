package br.ecosynergy_app.teams

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.ecosynergy_app.R

class TeamAdapter(private val teamsList: List<TeamsResponse>) :
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

        fun bind(team: TeamsResponse) {
            teamName.text = team.name
            teamHandle.text = "@${team.handle}"
            teamDescription.text = team.description
            teamImage.setImageResource(getDrawableForLetter(team.name.first()))

            btnInfo.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, TeamOverviewActivity::class.java)
                intent.putExtra("TEAM_ID", team.id)
                context.startActivity(intent)
            }
        }

        private fun getDrawableForLetter(letter: Char): Int {
            return when (letter.lowercaseChar()) {
                'a' -> R.drawable.a
                'b' -> R.drawable.b
                'c' -> R.drawable.c
                'd' -> R.drawable.d
                'e' -> R.drawable.e
                'f' -> R.drawable.f
                'g' -> R.drawable.g
                'h' -> R.drawable.h
                'i' -> R.drawable.i
                'j' -> R.drawable.j
                'k' -> R.drawable.k
                'l' -> R.drawable.l
                'm' -> R.drawable.m
                'n' -> R.drawable.n
                'o' -> R.drawable.o
                'p' -> R.drawable.p
                'q' -> R.drawable.q
                'r' -> R.drawable.r
                's' -> R.drawable.s
                't' -> R.drawable.t
                'u' -> R.drawable.u
                'v' -> R.drawable.v
                'w' -> R.drawable.w
                'x' -> R.drawable.x
                'y' -> R.drawable.y
                'z' -> R.drawable.z
                else -> R.drawable.default_image
            }
        }
    }
}
