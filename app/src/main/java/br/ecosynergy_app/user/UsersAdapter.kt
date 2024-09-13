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
import br.ecosynergy_app.teams.AddMembersBottomSheet
import br.ecosynergy_app.teams.RoleRequest
import br.ecosynergy_app.teams.TeamMembersFragment
import br.ecosynergy_app.teams.TeamsViewModel
import br.ecosynergy_app.teams.TeamsViewModelFactory
import com.google.android.material.snackbar.Snackbar

class UsersAdapter(
    private var usersList: MutableList<UserResponse>,
    private var teamId: Int,
    private var teamHandle: String?,
    private val teamsViewModel: TeamsViewModel,
    private val activity: FragmentActivity,
    private val fragment: AddMembersBottomSheet,
    private val memberIds: List<String>
) : RecyclerView.Adapter<UsersAdapter.ViewHolder>() {

    fun updateList(newList: MutableList<UserResponse>) {
        usersList = newList.sortedBy { it.fullName }.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.users_layout, parent, false)
        return ViewHolder(view, usersList, teamId, teamHandle, teamsViewModel, activity, fragment, memberIds)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(usersList[position])
    }

    override fun getItemCount(): Int = usersList.size

    class ViewHolder(
        itemView: View,
        private var usersList: MutableList<UserResponse>,
        private var teamId: Int,
        private var teamHandle: String?,
        private val teamsViewModel: TeamsViewModel,
        private val activity: FragmentActivity,
        private val fragment: AddMembersBottomSheet,
        private val memberIds: List<String>
    ) : RecyclerView.ViewHolder(itemView) {

        private val txtUsername: TextView = itemView.findViewById(R.id.txtUsername)
        private val txtFullname: TextView = itemView.findViewById(R.id.txtFullname)
        private val txtView: TextView = itemView.findViewById(R.id.txtView)
        private val imgUser: ImageView = itemView.findViewById(R.id.imgUser)
        private val btnInvite: ImageButton = itemView.findViewById(R.id.btnInvite)

        private var token: String? = ""
        private var userId: Int = 0
        private var username: String? = null
        private var memberId: Int = 0

        fun bind(user: UserResponse) {
            username = user.username
//            val sp: SharedPreferences = itemView.context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
//            token = sp.getString("accessToken", null)
//            userId = sp.getString("id", null)

            txtFullname.text = user.fullName
            txtUsername.text = "@$username"
            memberId = user.id

            imgUser.setImageResource(getDrawableForLetter(user.fullName.first()))

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

        private fun inviteUser(){
            teamsViewModel.addMember(token, teamId, memberId, RoleRequest("COMMON_USER"))
            btnInvite.visibility = View.GONE
            showSnackBar("Usuário adicionado com sucesso!", "FECHAR", R.color.greenDark)
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
