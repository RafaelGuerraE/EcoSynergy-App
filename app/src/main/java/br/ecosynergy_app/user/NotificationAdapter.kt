package br.ecosynergy_app.user

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.ecosynergy_app.R
import br.ecosynergy_app.room.invites.Invites
import br.ecosynergy_app.room.notifications.Notifications
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class NotificationAdapter(
    private var notifications: List<Notifications>,
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    private var notificationsList: List<Notifications> = notifications.sortedByDescending {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(it.time)?.time
    }

    // ViewHolder class to hold item views
    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgIcon: ImageView = itemView.findViewById(R.id.imgIcon)
        private val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
        private val txtSubtitle: TextView = itemView.findViewById(R.id.txtSubtitle)
        private val txtTime: TextView = itemView.findViewById(R.id.txtTime)
        private val linearNotification: LinearLayout = itemView.findViewById(R.id.linearNotification)

        // Bind notification data to views
        fun bind(notification: Notifications) {
            txtTitle.text = notification.title
            txtSubtitle.text = notification.subtitle
            txtTime.text = getDisplayTime(notification.time)

            // Set an icon based on the notification type (example)
            when (notification.type) {
                "fire" -> imgIcon.setImageResource(R.drawable.ic_fire)
                "invite" -> imgIcon.setImageResource(R.drawable.ic_invite)
                "greeting" -> imgIcon.setImageResource(R.drawable.ic_greetings)
                else -> imgIcon.setImageResource(R.drawable.ic_notification)
            }

            linearNotification.setOnClickListener {
                val context = itemView.context
                val i = Intent(context, NotificationActivity::class.java)
                context.startActivity(i)
            }
        }

        private fun getDisplayTime(notificationTime: String): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val parsedDate = formatter.parse(notificationTime)

            parsedDate?.let {
                val diffMillis = System.currentTimeMillis() - it.time

                return when {
                    diffMillis < TimeUnit.MINUTES.toMillis(1) -> "Just now"
                    diffMillis < TimeUnit.HOURS.toMillis(1) -> {
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
                        "Há $minutes minutos"
                    }
                    diffMillis < TimeUnit.DAYS.toMillis(1) -> {
                        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
                        "Há $hours horas"
                    }
                    diffMillis < TimeUnit.DAYS.toMillis(7) -> {
                        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)
                        "Há $days dias"
                    }
                    diffMillis < TimeUnit.DAYS.toMillis(30) -> {
                        val weeks = TimeUnit.MILLISECONDS.toDays(diffMillis) / 7
                        "Há $weeks semanas"
                    }
                    else -> {
                        val months = TimeUnit.MILLISECONDS.toDays(diffMillis) / 30
                        "Há $months meses"
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

    override fun getItemCount(): Int = notificationsList.size // Updated line

    fun updateData(newNotifications: List<Notifications>) {
        notifications = newNotifications
        notificationsList = notifications.sortedByDescending {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(it.time)?.time
        }
        notifyDataSetChanged()
    }
}
