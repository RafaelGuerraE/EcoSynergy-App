package br.ecosynergy_app.user

import android.app.AlertDialog
import android.content.Context
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
import br.ecosynergy_app.teams.RoleRequest
import br.ecosynergy_app.teams.TeamMembersFragment
import br.ecosynergy_app.teams.TeamsViewModel
import br.ecosynergy_app.teams.TeamsViewModelFactory
import com.google.android.material.snackbar.Snackbar

class MembersAdapter(
    private var membersList: List<UserResponse>,
    private var memberRoles: List<String>,
    private var currentUserRole: String?,
    private var teamId: String?,
    private var teamHandle: String?,
    private val teamsViewModel: TeamsViewModel,
    private val activity: FragmentActivity,
    private val fragment: TeamMembersFragment
) : RecyclerView.Adapter<MembersAdapter.ViewHolder>() {

    fun updateList(newList: List<UserResponse>, newRoles: List<String>) {
        membersList = newList.sortedBy { it.fullName }
        memberRoles = newRoles
        notifyDataSetChanged()
    }

    fun removeMember(memberId: String?) {
        membersList = membersList.filter { it.id.toString() != memberId }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.members_layout, parent, false)
        return ViewHolder(view, membersList, memberRoles, currentUserRole, teamId, teamHandle, teamsViewModel, activity,fragment)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(membersList[position], memberRoles[position])
    }

    override fun getItemCount(): Int = membersList.size

    class ViewHolder(
        itemView: View,
        private var membersList: List<UserResponse>,
        private var memberRoles: List<String>,
        private var currentUserRole: String?,
        private var teamId: String?,
        private var teamHandle: String?,
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

        private var token: String? = ""
        private var userId: String? = ""
        private var username: String? = null
        private var memberId: String? = null

        fun bind(user: UserResponse, role: String) {
            username = user.username
            val sp: SharedPreferences = itemView.context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
            token = sp.getString("accessToken", null)
            userId = sp.getString("id", null)

            txtUsername.text = "@$username"
            memberId = user.id
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

            imgUser.setImageResource(getDrawableForLetter(user.fullName.first()))

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

        private fun exitTeam(){
            val builder = AlertDialog.Builder(itemView.context)
            builder.setTitle("Você deseja sair da equipe atual?")
            builder.setMessage("Você será removido da equipe atual.")

            builder.setPositiveButton("Sim") { dialog, _ ->
                teamsViewModel.removeMember(token, teamId, userId)
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
                teamsViewModel.removeMember(token, teamId, memberId)
                dialog.dismiss()
                fragment.membersAdapter.removeMember(memberId)
                showSnackBar("Usuário removido com sucesso", "FECHAR", R.color.greenDark)
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
                        teamsViewModel.editMemberRole(token, teamHandle, memberId, RoleRequest("ADMINISTRATOR"))
                    }

                    1 -> {
                        teamsViewModel.editMemberRole(token, teamHandle, memberId, RoleRequest("COMMON_USER"))
                    }
                }
                dialog.dismiss()
                fragment.membersAdapter.updateList(membersList, memberRoles)
                showSnackBar("Cargo alterado com sucesso", "FECHAR", R.color.greenDark)
            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        private fun showSnackBar(message: String, action: String, bgTint: Int) {
            val rootView = itemView
            val snackBar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT)
                .setAction(action) {}
            snackBar.setBackgroundTint(ContextCompat.getColor(itemView.context, bgTint))
            snackBar.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
            snackBar.setActionTextColor(ContextCompat.getColor(itemView.context, R.color.white))
            snackBar.show()
        }
    }
}
