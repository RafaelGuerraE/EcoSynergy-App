package br.ecosynergy_app.home

import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import br.ecosynergy_app.R

class NotificationPreferencesActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var fireYes: CheckBox
    private lateinit var fireNo: CheckBox
    private lateinit var receivedInvitesYes: CheckBox
    private lateinit var receivedInvitesNo: CheckBox
    private lateinit var sentInvitesYes: CheckBox
    private lateinit var sentInvitesNo: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_preferences)

        btnBack = findViewById(R.id.btnBack)
        fireYes = findViewById(R.id.fireYes)
        fireNo = findViewById(R.id.fireNo)
        receivedInvitesYes = findViewById(R.id.receivedInvitesYes)
        receivedInvitesNo = findViewById(R.id.receivedInvitesNo)
        sentInvitesYes = findViewById(R.id.sentInvitesYes)
        sentInvitesNo = findViewById(R.id.sentInvitesNo)

        btnBack.setOnClickListener {
            finish()
        }

        setMutuallyExclusiveCheckboxes(fireYes, fireNo)
        setMutuallyExclusiveCheckboxes(receivedInvitesYes, receivedInvitesNo)
        setMutuallyExclusiveCheckboxes(sentInvitesYes, sentInvitesNo)
    }

    private fun setMutuallyExclusiveCheckboxes(checkBox1: CheckBox, checkBox2: CheckBox) {
        checkBox1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkBox2.isChecked = false
            }
        }
        checkBox2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkBox1.isChecked = false
            }
        }
    }
}
