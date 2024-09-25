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
import br.ecosynergy_app.home.HomeActivity
import br.ecosynergy_app.login.LoginActivity
import br.ecosynergy_app.teams.AddMembersBottomSheet
import br.ecosynergy_app.teams.RoleRequest
import br.ecosynergy_app.teams.TeamMembersFragment
import br.ecosynergy_app.teams.TeamsViewModel
import br.ecosynergy_app.teams.TeamsViewModelFactory
import com.google.android.material.snackbar.Snackbar

class UsersAdapter(
    private var usersList: MutableList<UserResponse>,
    private var teamId: Int,
    private var teamHandle: String,
    private val teamsViewModel: TeamsViewModel,
    private val memberIds: List<String>,
    private val accessToken: String,
    private val userId: Int,
    private val username: String
) : RecyclerView.Adapter<UsersAdapter.ViewHolder>() {

    fun updateList(newList: MutableList<UserResponse>) {
        usersList = newList.sortedBy { it.fullName }.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.users_layout, parent, false)
        return ViewHolder(view, usersList, teamId, teamHandle, teamsViewModel, memberIds, accessToken, userId, username)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(usersList[position])
    }

    override fun getItemCount(): Int = usersList.size

    class ViewHolder(
        itemView: View,
        private var usersList: MutableList<UserResponse>,
        private var teamId: Int,
        private var teamHandle: String,
        private val teamsViewModel: TeamsViewModel,
        private val memberIds: List<String>,
        private val accessToken: String,
        private val userId: Int,
        private var username: String
    ) : RecyclerView.ViewHolder(itemView) {

        private val txtUsername: TextView = itemView.findViewById(R.id.txtUsername)
        private val txtFullname: TextView = itemView.findViewById(R.id.txtFullname)
        private val txtView: TextView = itemView.findViewById(R.id.txtView)
        private val imgUser: ImageView = itemView.findViewById(R.id.imgUser)
        private val btnInvite: ImageButton = itemView.findViewById(R.id.btnInvite)
        private var memberId: Int = 0

        fun bind(user: UserResponse) {
            username = user.username

            txtFullname.text = user.fullName
            txtUsername.text = "@$username"
            memberId = user.id

            imgUser.setImageResource(HomeActivity().getDrawableForLetter(user.fullName.first()))

            Log.d("UsersAdapter", "MemberIDS: $memberIds")

            if (memberIds.contains(memberId.toString())) {
                btnInvite.visibility = View.GONE
            } else {
                btnInvite.visibility = View.VISIBLE
            }

            btnInvite.setOnClickListener {
                inviteUser()
            }
        }

        private fun inviteUser(){
            teamsViewModel.addMember(accessToken, teamId, memberId, RoleRequest("COMMON_USER"))
            btnInvite.visibility = View.GONE
            LoginActivity().showSnackBar("Usuário adicionado com sucesso!", "FECHAR", R.color.greenDark)
        }
    }
}
