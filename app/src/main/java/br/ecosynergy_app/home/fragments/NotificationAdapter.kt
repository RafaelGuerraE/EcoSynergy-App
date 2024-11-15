package br.ecosynergy_app.home.fragments

import android.content.Intent
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.ecosynergy_app.R
import br.ecosynergy_app.room.notifications.Notifications
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class NotificationAdapter(
    private var notifications: MutableList<Notifications>,
    private var accessToken: String,
    private var userId:Int,
    private val onNotificationAccessed: (Int) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    var notificationsList: MutableList<Notifications> = notifications.sortedByDescending {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(it.timestamp)?.time
    }.toMutableList()

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgIcon: ImageView = itemView.findViewById(R.id.imgIcon)
        private val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
        private val txtSubtitle: TextView = itemView.findViewById(R.id.txtSubtitle)
        private val txtTime: TextView = itemView.findViewById(R.id.txtTime)
        private val linearNotification: LinearLayout = itemView.findViewById(R.id.linearNotification)
        private val imgNew: ImageView = itemView.findViewById(R.id.imgNew)

        fun bind(notification: Notifications) {
            txtTitle.text = notification.title
            txtSubtitle.text = notification.subtitle
            txtTime.text = getDisplayTime(notification.timestamp)

            if (notification.read) {
                imgNew.visibility = View.GONE
                txtTitle.setTypeface(txtTitle.typeface, Typeface.NORMAL)
            } else {
                imgNew.visibility = View.VISIBLE
                txtTitle.setTypeface(txtTitle.typeface, Typeface.BOLD)
            }

            when (notification.type) {
                "fire" -> imgIcon.setImageResource(R.drawable.ic_fire)
                "invite" -> imgIcon.setImageResource(R.drawable.ic_invite)
                "greeting" -> imgIcon.setImageResource(R.drawable.ic_greetings)
                else -> imgIcon.setImageResource(R.drawable.ic_notification)
            }

            linearNotification.setOnClickListener {
                if (!notification.read) {
                    onNotificationAccessed(notification.id)
                }

                val context = itemView.context
                val i = Intent(context, NotificationActivity::class.java)
                i.apply {
                    putExtra("TIME", notification.timestamp)
                    putExtra("ACCESS_TOKEN", accessToken)
                    putExtra("USER_ID", userId)
                    putExtra("TYPE", notification.type)
                    putExtra("TEAM_ID", notification.teamId)
                    putExtra("INVITE_ID", notification.inviteId)
                }
                context.startActivity(i)
            }
        }

        private fun getDisplayTime(notificationTime: String): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val parsedDate = formatter.parse(notificationTime)

            parsedDate?.let {
                val diffMillis = System.currentTimeMillis() - it.time

                return when {
                    diffMillis < TimeUnit.MINUTES.toMillis(1) -> "agora mesmo"
                    diffMillis < TimeUnit.HOURS.toMillis(1) -> {
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
                        if (minutes == 1L) {
                            "há 1 minuto"
                        } else {
                            "há $minutes minutos"
                        }
                    }
                    diffMillis < TimeUnit.DAYS.toMillis(1) -> {
                        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
                        if (hours == 1L) {
                            "há 1 hora"
                        } else {
                            "há $hours horas"
                        }
                    }
                    diffMillis < TimeUnit.DAYS.toMillis(7) -> {
                        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)
                        if (days == 1L) {
                            "há 1 dia"
                        } else {
                            "há $days dias"
                        }
                    }
                    diffMillis < TimeUnit.DAYS.toMillis(30) -> {
                        val weeks = TimeUnit.MILLISECONDS.toDays(diffMillis) / 7
                        if (weeks == 1L) {
                            "há 1 semana"
                        } else {
                            "há $weeks semanas"
                        }
                    }
                    else -> {
                        val months = TimeUnit.MILLISECONDS.toDays(diffMillis) / 30
                        if (months == 1L) {
                            "há 1 mês"
                        } else {
                            "há $months meses"
                        }
                    }
                }
            }
            return notificationTime
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notifications_layout, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notificationsList[position])
    }

    override fun getItemCount(): Int = notificationsList.size

    fun updateData(newNotifications: List<Notifications>) {
        notificationsList.clear()
        notificationsList.addAll(newNotifications.sortedByDescending {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(it.timestamp)?.time
        })
        notifyDataSetChanged()
    }
}
