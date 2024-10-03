package br.ecosynergy_app.user

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.recyclerview.widget.RecyclerView
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.home.HomeActivity
import br.ecosynergy_app.login.LoginActivity
import br.ecosynergy_app.room.Members
import br.ecosynergy_app.teams.RoleRequest
import br.ecosynergy_app.teams.TeamMembersFragment
import br.ecosynergy_app.teams.TeamsViewModel
import br.ecosynergy_app.teams.TeamsViewModelFactory
import com.google.android.material.snackbar.Snackbar

class MembersAdapter(
    private var membersList: List<Members>,
    private var memberRoles: List<String>,
    private var currentUserRole: String?,
    private var teamId: Int,
    private var userId: Int,
    private var accessToken: String,
    private val teamsViewModel: TeamsViewModel,
    private val activity: FragmentActivity,
    private val fragment: TeamMembersFragment,
    private val memberIds: MutableList<Int>
) : RecyclerView.Adapter<MembersAdapter.ViewHolder>() {

    fun updateList(newList: List<Members>, newRoles: List<String>) {
        val pairedList = newList.zip(newRoles)
            .sortedBy { it.first.fullName }
        membersList = pairedList.map { it.first }
        memberRoles = pairedList.map { it.second }
        notifyDataSetChanged()
    }

    fun removeMember(memberId: Int) {
        memberIds.remove(memberId)
        membersList = membersList.filter { it.userId != memberId}
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.members_layout, parent, false)
        return ViewHolder(view, membersList, memberRoles, currentUserRole, teamId, userId, accessToken, teamsViewModel, activity,fragment)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(membersList[position], memberRoles[position])
    }

    override fun getItemCount(): Int = membersList.size

    class ViewHolder(
        itemView: View,
        private var membersList: List<Members>,
        private var memberRoles: List<String>,
        private var currentUserRole: String?,
        private var teamId: Int,
        private var userId: Int,
        private var accessToken: String,
        private val teamsViewModel: TeamsViewModel,
        private val activity: FragmentActivity,
        private val fragment: TeamMembersFragment
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

        fun bind(user: Members, role: String) {
            username = user.username
            memberId = user.userId

            txtUsername.text = "@$username"

            txtRole.text = when (role) {
                "ADMINISTRATOR" -> "Administrador"
                "COMMON_USER" -> "Membro"
                else -> "Outro Cargo"
            }

            if (userId == memberId){
                txtFullname.text = user.fullName + " (Você)"
                btnExitTeam.visibility = View.VISIBLE
            }else{
                txtFullname.text = user.fullName
                btnExitTeam.visibility = View.GONE
            }

            imgUser.setImageResource(HomeActivity().getDrawableForLetter(user.fullName.first()))

            if (currentUserRole == "ADMINISTRATOR" && userId != memberId) {
                btnEditRole.visibility = View.VISIBLE
                btnRemove.visibility = View.VISIBLE
            }
            else {
                btnEditRole.visibility = View.GONE
                btnRemove.visibility = View.GONE
            }

            btnRemove.setOnClickListener {
                removeUser()
            }

            btnEditRole.setOnClickListener {
                editUserRole()
            }

            btnExitTeam.setOnClickListener{
                exitTeam()
            }

            imgUser.setOnClickListener{
                val i = Intent(activity, UserInfoActivity::class.java)
                i.apply {
                    putExtra("USERNAME", user.username)
                    putExtra("FULLNAME", user.fullName)
                    putExtra("EMAIL", user.email)
                    putExtra("GENDER", user.gender)
                    putExtra("NATIONALITY", user.nationality)
                    putExtra("FULLNAME", user.fullName)
                    putExtra("CREATED", user.createdAt)
                }
                activity.startActivity(i)
            }
        }

        private fun exitTeam(){
            val builder = AlertDialog.Builder(itemView.context)
            builder.setTitle("Você deseja sair da equipe atual?")
            builder.setMessage("Você será removido da equipe atual.")

            builder.setPositiveButton("Sim") { dialog, _ ->
                teamsViewModel.removeMember(accessToken, teamId, userId)
                dialog.dismiss()
                activity.finish()
            }

            builder.setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        private fun removeUser(){
            val builder = AlertDialog.Builder(itemView.context)
            builder.setTitle("Você deseja remover @$username?")
            builder.setMessage("Este membro será removido da equipe atual.")

            builder.setPositiveButton("Sim") { dialog, _ ->
                teamsViewModel.removeMember(accessToken, teamId, memberId)
                dialog.dismiss()
                fragment.membersAdapter.removeMember(memberId)
                LoginActivity().showSnackBar("Usuário removido com sucesso", "FECHAR", R.color.greenDark)
            }

            builder.setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        private fun editUserRole() {
            val items = arrayOf("Administrador", "Membro")

            val builder = AlertDialog.Builder(itemView.context)
            builder.setTitle("Alterar o cargo do membro @$username")

            builder.setItems(items) { dialog, which ->

                when (which) {
                    0 -> {
                        teamsViewModel.editMemberRole(accessToken, teamId, memberId, RoleRequest("ADMINISTRATOR"))
                    }

                    1 -> {
                        teamsViewModel.editMemberRole(accessToken, teamId, memberId, RoleRequest("COMMON_USER"))
                    }
                }
                dialog.dismiss()
                fragment.membersAdapter.updateList(membersList, memberRoles)
                LoginActivity().showSnackBar("Cargo alterado com sucesso", "FECHAR", R.color.greenDark)
            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }
}
