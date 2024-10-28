package br.ecosynergy_app.user

import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import br.ecosynergy_app.R
import br.ecosynergy_app.home.HomeActivity
import br.ecosynergy_app.room.teams.Members
import br.ecosynergy_app.room.teams.TeamsRepository
import br.ecosynergy_app.teams.viewmodel.RoleRequest
import br.ecosynergy_app.teams.TeamMembersActivity
import br.ecosynergy_app.teams.viewmodel.TeamsViewModel

class MembersAdapter(
    private var membersList: MutableList<Members>,
    private var memberRoles: MutableList<String>,
    private var currentUserRole: String?,
    private var teamId: Int,
    private var userId: Int,
    private var accessToken: String,
    private val teamsViewModel: TeamsViewModel,
    private val activity: FragmentActivity,
    private val memberIds: MutableList<Int>
) : RecyclerView.Adapter<MembersAdapter.ViewHolder>() {

    fun updateList(newList: List<Members>, newRoles: List<String>) {
        val pairedList = newList.zip(newRoles)
            .sortedBy { it.first.fullName }
        membersList = pairedList.map { it.first }.toMutableList()
        memberRoles = pairedList.map { it.second }.toMutableList()
        notifyDataSetChanged()
    }

    fun removeMember(memberId: Int) {
        memberIds.remove(memberId)
        membersList = membersList.filter { it.userId != memberId }.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.members_layout, parent, false)
        return ViewHolder(view, membersList, memberRoles, currentUserRole, teamId, userId, accessToken, teamsViewModel, activity)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(membersList[position], memberRoles[position], position)
    }

    override fun getItemCount(): Int = membersList.size

    class ViewHolder(
        itemView: View,
        private var membersList: MutableList<Members>,
        private var memberRoles: MutableList<String>,
        private var currentUserRole: String?,
        private var teamId: Int,
        private var userId: Int,
        private var accessToken: String,
        private val teamsViewModel: TeamsViewModel,
        private val activity: FragmentActivity
    ) : RecyclerView.ViewHolder(itemView) {

        private val txtUsername: TextView = itemView.findViewById(R.id.txtUsername)
        private val txtFullname: TextView = itemView.findViewById(R.id.txtFullname)
        private val txtRole: TextView = itemView.findViewById(R.id.txtRole)
        private val imgUser: ImageView = itemView.findViewById(R.id.imgUser)
        private val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemove)
        private val btnEditRole: ImageButton = itemView.findViewById(R.id.btnEditRole)
        private val btnExitTeam: ImageButton = itemView.findViewById(R.id.btnExitTeam)

        private var username: String = ""
        private var memberId: Int = 0

        fun bind(user: Members, role: String, position: Int) {
            username = user.username
            memberId = user.userId

            txtUsername.text = "@$username"
            txtRole.text = when (role) {
                "FOUNDER" -> "Fundador"
                "ADMINISTRATOR" -> "Administrador"
                "COMMON_USER" -> "Membro"
                else -> "Outro Cargo"
            }

            if (userId == memberId) {
                txtFullname.text = "${user.fullName} (Você)"
                btnExitTeam.visibility = View.VISIBLE
            } else {
                txtFullname.text = user.fullName
                btnExitTeam.visibility = View.GONE
            }

            imgUser .setImageResource(HomeActivity().getDrawableForLetter(user.fullName.first()))

            if ((currentUserRole == "ADMINISTRATOR" || currentUserRole == "FOUNDER") && userId != memberId && user.role != "FOUNDER") {
                btnEditRole.visibility = View.VISIBLE
                btnRemove.visibility = View.VISIBLE
            }else {
                btnEditRole.visibility = View.GONE
                btnRemove.visibility = View.GONE
            }



            btnRemove.setOnClickListener {
                removeUser()
            }

            btnEditRole.setOnClickListener {
                editUserRole(txtRole, position)
            }

            btnExitTeam.setOnClickListener {
                exitTeam()
            }

            imgUser.setOnClickListener {
                val i = Intent(activity, UserInfoActivity::class.java)
                i.apply {
                    putExtra("USERNAME", user.username)
                    putExtra("FULLNAME", user.fullName)
                    putExtra("EMAIL", user.email)
                    putExtra("GENDER", user.gender)
                    putExtra("NATIONALITY", user.nationality)
                    putExtra("CREATED", user.createdAt)
                }
                activity.startActivity(i)
            }
        }

        private fun exitTeam() {
            val builder = AlertDialog.Builder(itemView.context)
            builder.setTitle("Você deseja sair da equipe atual?")
            builder.setMessage("Você será removido da equipe atual.")

            builder.setPositiveButton("Sim") { dialog, _ ->
                teamsViewModel.removeMember(accessToken, teamId, userId)
                teamsViewModel.deleteTeamFromDB(teamId)
                dialog.dismiss()
                activity.setResult(FragmentActivity.RESULT_OK, Intent().apply {
                    putExtra("TEAM_ID", teamId)
                })
                activity.finish()
            }

            builder.setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        private fun removeUser() {
            val builder = AlertDialog.Builder(itemView.context)
            builder.setTitle("Você deseja remover @$username?")
            builder.setMessage("Este membro será removido da equipe atual.")

            builder.setPositiveButton("Sim") { dialog, _ ->
                teamsViewModel.removeMember(accessToken, teamId, memberId)
                dialog.dismiss()
                (activity as TeamMembersActivity).membersAdapter.removeMember(memberId)
            }

            builder.setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }

            showToast("Usuário removido com sucesso")

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        private fun editUserRole(textView: TextView, position: Int) {
            val items = arrayOf("Administrador", "Membro")

            val builder = AlertDialog.Builder(itemView.context)
            builder.setTitle("Alterar o cargo do membro @$username")

            builder.setItems(items) { dialog, which ->
                val newRole = when (which) {
                    0 -> "ADMINISTRATOR"
                    1 -> "COMMON_USER"
                    else -> memberRoles[position]
                }

                memberRoles[position] = newRole

                teamsViewModel.editMemberRole(accessToken, teamId, memberId, RoleRequest(newRole))

                textView.text = when (newRole) {
                    "ADMINISTRATOR" -> "Administrador"
                    "COMMON_USER" -> "Membro"
                    else -> "Outro Cargo"
                }

                // Notify the adapter to refresh the list
                (activity as TeamMembersActivity).membersAdapter.updateList(membersList, memberRoles)
                showToast("Cargo alterado com sucesso")
                dialog.dismiss()
            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        private fun showToast(message: String) {
            Toast.makeText(itemView.context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
