package br.ecosynergy_app.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import br.ecosynergy_app.R
import com.google.android.material.snackbar.Snackbar

class MembersAdapter(private var membersList: List<UserResponse>, private var memberRoles: List<String>) :
    RecyclerView.Adapter<MembersAdapter.ViewHolder>() {

    fun updateList(newList: List<UserResponse>, newRoles: List<String>) {
        membersList = newList
        memberRoles = newRoles
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.members_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       holder.bind(membersList[position], memberRoles[position])
    }

    override fun getItemCount(): Int = membersList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtUsername: TextView = itemView.findViewById(R.id.txtUsername)
        private val txtFullname: TextView = itemView.findViewById(R.id.txtFullname)
        private val txtRole: TextView = itemView.findViewById(R.id.txtRole)
        private val imgUser: ImageView = itemView.findViewById(R.id.imgUser)
        private val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemove)

        fun bind(user: UserResponse, role: String) {
            txtUsername.text = "@${user.username}"
            txtFullname.text = user.fullName

            imgUser.setImageResource(getDrawableForLetter(user.fullName.first()))
            when(role){
                "ADMINISTRATOR" -> txtRole.text = "Administrador"
                "COMMON_USER" -> txtRole.text = "Membro"
                else -> "Outro Cargo"
            }
            btnRemove.setOnClickListener {

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
