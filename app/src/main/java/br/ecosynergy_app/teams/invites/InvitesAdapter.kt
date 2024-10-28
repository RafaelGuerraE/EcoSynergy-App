package br.ecosynergy_app.teams.invites

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import br.ecosynergy_app.R
import br.ecosynergy_app.home.HomeActivity
import br.ecosynergy_app.room.invites.Invites
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class InvitesAdapter(private val invites: List<Invites>) :
    RecyclerView.Adapter<InvitesAdapter.ViewHolder>() {

    private val invitesList: List<Invites> = invites.sortedByDescending {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(it.createdAt)?.time
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.invites_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(invitesList[position])
    }

    override fun getItemCount(): Int = invitesList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recipientTextView: TextView = itemView.findViewById(R.id.txtRecipient)
        private val senderTextView: TextView = itemView.findViewById(R.id.txtSender)
        private val imgStatus: ImageView = itemView.findViewById(R.id.imgStatus)
        private val statusTextView: TextView = itemView.findViewById(R.id.txtStatus)
        private val profileImageView: ImageView = itemView.findViewById(R.id.imgTeam)
        private val overlayImageView: ImageView = itemView.findViewById(R.id.imgOverlay)
        private val btnInfo: ImageButton = itemView.findViewById(R.id.btnInfo)

        fun bind(invite: Invites) {

            val date = getDisplayTime(invite.createdAt)

            recipientTextView.text = "Convite para @${invite.recipientUsername}"
            senderTextView.text = "Enviado por @${invite.senderUsername} $date"
            statusTextView.text = when (invite.status) {
                "ACCEPTED" -> "Convite aceito"
                "PENDING" -> "Convite pendente"
                "DECLINED" -> "Convite recusado"
                else -> "Status desconhecido"
            }

            imgStatus.setImageResource(
                when (invite.status) {
                    "ACCEPTED" -> R.drawable.ic_successful
                    "PENDING" -> R.drawable.ic_pending
                    "DECLINED" -> R.drawable.ic_error
                    else -> R.drawable.ic_pending
                }
            )

            setIconTint(invite.status)

            profileImageView.setImageResource(HomeActivity().getDrawableForLetter(invite.recipientUsername.first()))
            overlayImageView.setImageResource(HomeActivity().getDrawableForLetter(invite.senderUsername.first()))

            btnInfo.setOnClickListener {
                showInfoDialog(invite)
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
                            "há 1 minuto"  // Singular form
                        } else {
                            "há $minutes minutos"  // Plural form
                        }
                    }
                    diffMillis < TimeUnit.DAYS.toMillis(1) -> {
                        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
                        if (hours == 1L) {
                            "1 hora"  // Singular form
                        } else {
                            "há $hours horas"  // Plural form
                        }
                    }
                    diffMillis < TimeUnit.DAYS.toMillis(7) -> {
                        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)
                        if (days == 1L) {
                            "há 1 dia"  // Singular form
                        } else {
                            "há $days dias"  // Plural form
                        }
                    }
                    diffMillis < TimeUnit.DAYS.toMillis(30) -> {
                        val weeks = TimeUnit.MILLISECONDS.toDays(diffMillis) / 7
                        if (weeks == 1L) {
                            "há 1 semana"  // Singular form
                        } else {
                            "há $weeks semanas"  // Plural form
                        }
                    }
                    else -> {
                        val months = TimeUnit.MILLISECONDS.toDays(diffMillis) / 30
                        if (months == 1L) {
                            "há 1 mês"  // Singular form
                        } else {
                            "há $months meses"  // Plural form
                        }
                    }
                }
            }
            return notificationTime
        }

        private fun showInfoDialog(invite: Invites) {
            val context = itemView.context
            val builder = AlertDialog.Builder(context)

            val formatterInput = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val formatterOutput = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val parsedDate = formatterInput.parse(invite.createdAt)
            val formattedDate = parsedDate?.let { formatterOutput.format(it) } ?: invite.createdAt

            val status = when(invite.status){
                "PENDING"-> "Pendente"
                "DECLIED"-> "Negado"
                "ACCEPTED" -> "Aceito"
                else -> "Erro"
            }

            builder.setTitle("Detalhes do Convite")
            builder.setMessage("Convite para @${invite.recipientUsername} \nEnviado por @${invite.senderUsername}. \nStatus: $status \nEm $formattedDate")
            builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }

            val alertDialog = builder.create()
            alertDialog.show()
        }


        private fun setIconTint(status: String) {
            val color = when (status) {
                "ACCEPTED" -> R.color.greenDark
                "PENDING" -> R.color.black
                "DECLINED" -> R.color.red
                else -> R.color.black
            }
            imgStatus.setColorFilter(itemView.context.getColor(color), android.graphics.PorterDuff.Mode.SRC_IN)
        }
    }
}
