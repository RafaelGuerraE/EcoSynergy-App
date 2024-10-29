package br.ecosynergy_app.home

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.user.UserRepository
import br.ecosynergy_app.user.viewmodel.UserViewModel
import br.ecosynergy_app.user.viewmodel.UserViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import java.util.Locale

class AppSettingsActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel

    private var userUsername: String = ""
    private var accessToken: String = ""

    private lateinit var btnBack: ImageButton
    private lateinit var areaAppearance: LinearLayout
    private lateinit var areaPassword: LinearLayout
    private lateinit var areaNotifications: LinearLayout
    private lateinit var areaMeasure: LinearLayout
    private lateinit var areaPermissions: LinearLayout
    private lateinit var areaLanguage: LinearLayout
    private lateinit var areaHelp: LinearLayout

    private lateinit var txtMeasure: TextView
    private lateinit var txtLanguage: TextView

    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("AppSettings", MODE_PRIVATE)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appsetings)

        val userRepository = UserRepository(AppDatabase.getDatabase(this).userDao())
        userViewModel = ViewModelProvider(this, UserViewModelFactory(RetrofitClient.userService, userRepository))[UserViewModel::class.java]

        btnBack = findViewById(R.id.btnBack)
        areaAppearance = findViewById(R.id.areaAppearance)
        areaPassword = findViewById(R.id.areaPassword)
        areaNotifications = findViewById(R.id.areaNotifications)
        areaMeasure = findViewById(R.id.areaMeasure)
        areaPermissions = findViewById(R.id.areaPermissions)
        areaLanguage = findViewById(R.id.areaLanguage)
        areaHelp = findViewById(R.id.areaHelp)

        txtMeasure = findViewById(R.id.txtMeasure)
        txtLanguage = findViewById(R.id.txtLanguage)

        fetchUserData()

        btnBack.setOnClickListener { finish() }

        areaAppearance.setOnClickListener {
            manageThemes()
        }

        areaPassword.setOnClickListener {
            resetPassword()
        }

        areaNotifications.setOnClickListener {
            val i = Intent(this, NotificationPreferencesActivity::class.java)
            startActivity(i)
        }

        areaMeasure.setOnClickListener {
            showMeasureOptionsDialog()
        }

        areaPermissions.setOnClickListener {
            val i = Intent(this, PermissionsActivity::class.java)
            startActivity(i)
        }

        areaLanguage.setOnClickListener{
            showLanguageOptionsDialog()
        }

        areaHelp.setOnClickListener{
            val i = Intent(this, HelpActivity::class.java)
            startActivity(i)
        }
    }

    private fun fetchUserData() {
        userViewModel.getUserInfoFromDB {
            userViewModel.userInfo.observe(this) { userInfo ->
                userUsername = userInfo.username
                accessToken = userInfo.accessToken
            }
        }
    }

    private fun showMeasureOptionsDialog() {
        val options = arrayOf("ppm (Partes por Milhão)", "Toneladas", "ppb (Partes por Bilhão)")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecione a unidade de medida")
        builder.setSingleChoiceItems(options, 1) { dialog, which ->
            val selectedMeasure = options[which]
            txtMeasure.text = selectedMeasure
            // Save the selected measure if needed
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun showLanguageOptionsDialog() {
        val options = arrayOf("Português", "Inglês")
        val languageCodes = arrayOf("pt", "en")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecione o idioma")
        builder.setSingleChoiceItems(options, 0) { dialog, which ->
            val selectedLanguage = options[which]
            txtLanguage.text = selectedLanguage
            // Save the selected language
            with(sharedPreferences.edit()) {
                putString("language", selectedLanguage)
                apply()
            }
            // Apply language change
            setLocale(languageCodes[which])
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        // Refresh the activity to apply changes
        recreate()
    }

    private fun manageThemes(){
        val items = arrayOf("Padrão do Sistema", "Claro", "Escuro")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecione o tema desejado")

        builder.setItems(items) { dialog, which ->
            val editor = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE).edit()

            when (which) {
                0 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    editor.putString("theme", "system")
                }
                1 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    editor.putString("theme", "light")
                }
                2 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    editor.putString("theme", "dark")
                }
            }
            editor.apply()
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun resetPassword() {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.dialog_reset_password, null)

        val etNewPassword = view.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmNewPassword = view.findViewById<TextInputEditText>(R.id.etConfirmNewPassword)
        val errorMessage = view.findViewById<LinearLayout>(R.id.errorMessage)
        val txtError = view.findViewById<TextView>(R.id.txtError)


        etNewPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validatePasswords(etNewPassword, etConfirmNewPassword, txtError, errorMessage)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        etConfirmNewPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validatePasswords(etNewPassword, etConfirmNewPassword, txtError, errorMessage)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        val builder = AlertDialog.Builder(this)
        builder.setView(view)
            .setTitle("Alterar minha senha")
            .setPositiveButton("Confirmar") { dialog, _ ->
                val newPassword = etNewPassword.text.toString()
                val confirmNewPassword = etConfirmNewPassword.text.toString()

                if (newPassword.isBlank() || confirmNewPassword.isBlank()) {
                    showSnackBar("Erro: os campos não podem estar vazios", "FECHAR", R.color.red, this)
                } else if (newPassword != confirmNewPassword) {
                    showSnackBar("Erro: as senhas devem se corresponder", "FECHAR", R.color.red, this)
                } else {
                    userViewModel.resetPassword(userUsername, newPassword, accessToken)
                    showSnackBar("Senha alterada com sucesso", "FECHAR", R.color.greenDark, this)
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun validatePasswords(
        etNewPassword: TextInputEditText,
        etConfirmNewPassword: TextInputEditText,
        txtError: TextView,
        errorMessage:LinearLayout
    ) {
        val newPassword = etNewPassword.text.toString()
        val confirmNewPassword = etConfirmNewPassword.text.toString()

        when {
            newPassword.isBlank() -> {
                errorMessage.visibility = View.VISIBLE
                txtError.text = "As senhas não podem estar em branco"
            }

            newPassword != confirmNewPassword -> {
                errorMessage.visibility = View.VISIBLE
                txtError.text = "As senhas não correspondem"
            }

            else -> {
                errorMessage.visibility = View.INVISIBLE
            }
        }


    }

    private fun showSnackBar(message: String, action: String, bgTint: Int, context: Context) {
        val rootView = findViewById<View>(android.R.id.content)
        val snackBar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT)
            .setAction(action) {}
        snackBar.setBackgroundTint(ContextCompat.getColor(context, bgTint))
        snackBar.setTextColor(ContextCompat.getColor(context, R.color.white))
        snackBar.setActionTextColor(ContextCompat.getColor(context, R.color.white))
        snackBar.show()
    }
}
