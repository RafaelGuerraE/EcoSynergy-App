package br.ecosynergy_app.home

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.user.UserRepository
import br.ecosynergy_app.user.UpdatePreferencesRequest
import br.ecosynergy_app.user.viewmodel.UserViewModel
import br.ecosynergy_app.user.viewmodel.UserViewModelFactory
import kotlinx.coroutines.launch

class NotificationPreferencesActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel

    private var accessToken: String = ""
    private var userId: Int = 0
    private var interval: Int = 0

    private lateinit var btnBack: ImageButton
    private lateinit var content: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var switchFire: SwitchCompat
    private lateinit var switchInvitesSent: SwitchCompat
    private lateinit var switchInvitesReceive: SwitchCompat
    private lateinit var switchGoals: SwitchCompat

    private lateinit var txtInterval: TextView
    private lateinit var btnInterval : LinearLayout
    private lateinit var linearFireInterval: LinearLayout

    private val intervalOptions = listOf(5, 10, 15, 20, 30)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_preferences)

        val userRepository = UserRepository(AppDatabase.getDatabase(applicationContext).userDao())
        userViewModel = ViewModelProvider(this, UserViewModelFactory(RetrofitClient.userService, userRepository))[UserViewModel::class.java]

        accessToken = intent.getStringExtra("ACCESS_TOKEN")?: ""

        btnBack = findViewById(R.id.btnBack)
        content = findViewById(R.id.content)
        progressBar = findViewById(R.id.progressBar)
        switchFire = findViewById(R.id.switchFire)
        switchInvitesSent = findViewById(R.id.switchInvitesSent)
        switchInvitesReceive = findViewById(R.id.switchInvitesReceive)
        switchGoals = findViewById(R.id.switchGoals)

        linearFireInterval = findViewById(R.id.linearFireInterval)
        btnInterval = findViewById(R.id.btnInterval)
        txtInterval = findViewById(R.id.txtInterval)

        linearFireInterval.visibility = View.GONE

        content.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        btnBack.setOnClickListener { finish() }


        btnInterval.setOnClickListener{
            showIntervalSelectionDialog()
        }

        getNotificationPreferencesByUser(accessToken){
            setupSwitchColors(switchFire, "fireDetection")
            setupSwitchColors(switchInvitesSent, "inviteStatus")
            setupSwitchColors(switchInvitesReceive, "inviteReceived")
            setupSwitchColors(switchGoals, "teamGoalReached")
            content.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }

    private fun showIntervalSelectionDialog() {
        val intervals = intervalOptions.map { "$it minutos" }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Selecione o intervalo")
            .setItems(intervals) { _, which ->
                interval = intervalOptions[which]
                txtInterval.text = "$interval minutos"
                val updatePreferencesRequest = UpdatePreferencesRequest(
                    userId = userId,
                    fireDetection = switchFire.isChecked,
                    fireIntervalMinutes = interval,
                    inviteStatus = switchInvitesSent.isChecked,
                    inviteReceived = switchInvitesReceive.isChecked,
                    teamGoalReached = switchGoals.isChecked,
                    platform = "ANDROID"
                )
                updateNotificationPreferences(updatePreferencesRequest, accessToken) {
                    Log.d("NotificationPreferences", "Interval preference updated to $interval")
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()
            .show()
    }

    private fun setupSwitchColors(switch: SwitchCompat, preferenceKey: String) {
        updateSwitchColors(switch, switch.isChecked)

        switch.setOnCheckedChangeListener { _, isChecked ->

            linearFireInterval.visibility = if (switchFire.isChecked) View.VISIBLE else View.GONE
            updateSwitchColors(switch, isChecked)
            val updatePreferencesRequest = UpdatePreferencesRequest(
                userId = userId,
                fireDetection = switchFire.isChecked,
                fireIntervalMinutes = interval,
                inviteStatus = switchInvitesSent.isChecked,
                inviteReceived = switchInvitesReceive.isChecked,
                teamGoalReached = switchGoals.isChecked,
                platform = "ANDROID"
            )
            updateNotificationPreferences(updatePreferencesRequest, accessToken) {
                Log.d("NotificationPreferences", "$preferenceKey preference updated to $isChecked")
            }
        }
    }

    private fun updateSwitchColors(switch: SwitchCompat, isChecked: Boolean) {
        if (isChecked) {
            switch.thumbTintList = ContextCompat.getColorStateList(this, R.color.greenDark)
            switch.trackTintList = ContextCompat.getColorStateList(this, R.color.green_50)
        } else {
            switch.thumbTintList = ContextCompat.getColorStateList(this, R.color.grayDark)
            switch.trackTintList = ContextCompat.getColorStateList(this, R.color.gray)
        }
    }

    private fun getNotificationPreferencesByUser(accessToken: String, onComplete: () -> Unit) {
        userViewModel.getNotificationPreferencesByUser(accessToken) {
            val preferences = userViewModel.preferences.value
            if (preferences != null) {

                userId = preferences.userId

                switchFire.isChecked = preferences.fireDetection
                switchInvitesSent.isChecked = preferences.inviteStatus
                switchInvitesReceive.isChecked = preferences.inviteReceived
                switchGoals.isChecked = preferences.teamGoalReached

                txtInterval.text = if(preferences.fireIntervalMinutes == 0) "Selecione" else preferences.fireIntervalMinutes.toString() + " minutos"
                linearFireInterval.visibility = if (preferences.fireDetection) View.VISIBLE else View.GONE
            }
            onComplete()
        }
    }

    private fun updateNotificationPreferences(updatePreferencesRequest: UpdatePreferencesRequest, accessToken: String, onComplete: () -> Unit) {
        userViewModel.updateNotificationPreferences(updatePreferencesRequest, accessToken){}
        onComplete()
    }

}
