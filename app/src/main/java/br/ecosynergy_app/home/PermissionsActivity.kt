package br.ecosynergy_app.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import br.ecosynergy_app.R

class PermissionsActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton

    private lateinit var spinnerStorage: Spinner
    private lateinit var spinnerNotification: Spinner

    private var isNotificationGranted: Boolean = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("PermissionsActivity", "Permission Granted")
            } else {
                Log.d("PermissionsActivity", "Permission Denied")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions)

        btnBack = findViewById(R.id.btnBack)
        spinnerStorage = findViewById(R.id.spinnerStorage)
        spinnerNotification = findViewById(R.id.spinnerNotification)

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    spinnerNotification.setSelection(0)
                } else {
                    spinnerNotification.setSelection(1)
                }
            }

        val isPermissionGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (isPermissionGranted) {
            spinnerNotification.setSelection(0)
        } else {
            spinnerNotification.setSelection(1)
        }

        spinnerStorage.isEnabled = false
        spinnerStorage.isClickable = false
        spinnerStorage.isEnabled = false

        btnBack.setOnClickListener{ finish() }

        spinnerNotification.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedOption = parent.getItemAtPosition(position).toString()
                if (selectedOption == "Ativado") {
                    handlePermission()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
    private fun handlePermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {}
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}