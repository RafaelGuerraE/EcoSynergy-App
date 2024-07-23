package br.ecosynergy_app.home

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import br.ecosynergy_app.R
import br.ecosynergy_app.register.Nationality
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class UserSettingsActivity : AppCompatActivity() {

    private val userViewModel: UserViewModel by viewModels()
    private var gender: Int = 0

    private var userId: Long = 0
    private var userUsername: String = ""
    private var userFullname: String = ""
    private var userEmail: String = ""
    private var userNationality: String = ""
    private var userGender: String = ""

    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var overlayView: View

    private var token: String? = ""

    private var isEditingUsername = false
    private var isEditingFullname = false
    private var isEditingEmail = false
    private var isEditingGender = false
    private var isEditingNationality = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usersettings)

        val sharedPreferences: SharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        token =  sharedPreferences.getString("accessToken", null)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnEditUsername = findViewById<ImageButton>(R.id.btnEditUsername)
        val btnEditFullname = findViewById<ImageButton>(R.id.btnEditFullname)
        val btnEditEmail = findViewById<ImageButton>(R.id.btnEditEmail)
        val btnEditGender = findViewById<ImageButton>(R.id.btnEditGender)
        val btnEditNationality = findViewById<ImageButton>(R.id.btnEditNationality)
        val txtUsername = findViewById<TextInputEditText>(R.id.txtUsername)
        val txtFullname = findViewById<TextInputEditText>(R.id.txtFullname)
        val txtEmail = findViewById<TextInputEditText>(R.id.txtEmail)
        val txtGender = findViewById<Spinner>(R.id.txtGender)
        val txtNationality = findViewById<AutoCompleteTextView>(R.id.txtNationality)
        val btnPassword = findViewById<Button>(R.id.btnPassword)
        val btnDelete = findViewById<Button>(R.id.btnDelete)

        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        overlayView = findViewById(R.id.overlayView)

        val nationalities = loadNationalities()
        val nationalityNames = nationalities.map { it.nationality }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nationalityNames)
        txtNationality.setAdapter(adapter)

        txtGender.isEnabled = false

        txtNationality.setTextColor(ContextCompat.getColor(this, R.color.disabled))

        loadingProgressBar.visibility = View.VISIBLE
        overlayView.visibility = View.VISIBLE

        fetchUserData()

        fun setEditButtons(button:ImageButton) {
            when (button) {
                btnEditUsername -> {
                    btnEditUsername.isEnabled = true
                    btnEditFullname.isEnabled = false
                    btnEditEmail.isEnabled = false
                    btnEditGender.isEnabled = false
                    btnEditNationality.isEnabled = false
                }
                btnEditFullname -> {
                    btnEditUsername.isEnabled = false
                    btnEditFullname.isEnabled = true
                    btnEditEmail.isEnabled = false
                    btnEditGender.isEnabled = false
                    btnEditNationality.isEnabled = false
                }
                btnEditEmail -> {
                    btnEditUsername.isEnabled = false
                    btnEditFullname.isEnabled = false
                    btnEditEmail.isEnabled = true
                    btnEditGender.isEnabled = false
                    btnEditNationality.isEnabled = false
                }
                btnEditGender -> {
                    btnEditUsername.isEnabled = false
                    btnEditFullname.isEnabled = false
                    btnEditEmail.isEnabled = false
                    btnEditGender.isEnabled = true
                    btnEditNationality.isEnabled = false
                }
                btnEditNationality -> {
                    btnEditUsername.isEnabled = false
                    btnEditFullname.isEnabled = false
                    btnEditEmail.isEnabled = false
                    btnEditGender.isEnabled = false
                    btnEditNationality.isEnabled = true
                }
            }
        }

        fun enableAllButtons() {
            btnEditUsername.isEnabled = true
            btnEditFullname.isEnabled = true
            btnEditEmail.isEnabled = true
            btnEditGender.isEnabled = true
            btnEditNationality.isEnabled = true
        }

        btnEditUsername.setOnClickListener{
            toggleEditMode(txtUsername, btnEditUsername, isEditingUsername)
            setEditButtons(btnEditUsername)
            isEditingUsername = !isEditingUsername
            if(!isEditingUsername){
                userUsername = txtUsername.text.toString()
                updateUserData()
                enableAllButtons()
                showSnackBar("Nome de usuário alterado com sucesso","FECHAR",R.color.greenDark)
            }
        }

        btnEditFullname.setOnClickListener{
            toggleEditMode(txtFullname, btnEditFullname, isEditingFullname)
            setEditButtons(btnEditFullname)
            isEditingFullname = !isEditingFullname
            if(!isEditingFullname){
                userFullname = txtFullname.text.toString()
                updateUserData()
                enableAllButtons()
                showSnackBar("Nome completo alterado com sucesso","FECHAR",R.color.greenDark)
            }
        }

        btnEditEmail.setOnClickListener{
            toggleEditMode(txtEmail, btnEditEmail, isEditingEmail)
            setEditButtons(btnEditEmail)
            isEditingEmail = !isEditingEmail
            if(!isEditingEmail){
                userEmail = txtEmail.text.toString()
                updateUserData()
                enableAllButtons()
                showSnackBar("Email alterado com sucesso","FECHAR",R.color.greenDark)
            }
        }

        btnEditGender.setOnClickListener{
            toggleEditMode(txtGender, btnEditGender, isEditingGender)
            setEditButtons(btnEditGender)
            isEditingGender = !isEditingGender
            if(!isEditingGender){
                userGender = txtGender.selectedItem.toString()
                updateUserData()
                enableAllButtons()
                showSnackBar("Genêro alterado com sucesso","FECHAR",R.color.greenDark)
            }
        }

        fun getTextColorPrimary(context: Context): Int {
            val typedValue = TypedValue()
            val theme = context.theme
            theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
            return typedValue.data
        }

        fun setTextColorPrimary(autoCompleteTextView: AutoCompleteTextView) {
            val colorPrimary = getTextColorPrimary(autoCompleteTextView.context)
            autoCompleteTextView.setTextColor(colorPrimary)
        }

        btnEditNationality.setOnClickListener{
            toggleEditMode(txtNationality, btnEditNationality, isEditingNationality)
            setEditButtons(btnEditNationality)
            setTextColorPrimary(txtNationality)
            isEditingNationality = !isEditingNationality
            if(!isEditingNationality){
                userNationality = txtNationality.text.toString()
                updateUserData()
                enableAllButtons()
                showSnackBar("Nacionalidade alterada com sucesso","FECHAR",R.color.greenDark)
                txtNationality.setTextColor(ContextCompat.getColor(this, R.color.disabled))
            }
        }

        btnDelete.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirme sua Ação")
            builder.setMessage("Você deseja excluir sua conta?")

            builder.setPositiveButton("Sim") { dialog, _ ->
                deleteUserData()
                dialog.dismiss()
            }

            builder.setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        btnPassword.setOnClickListener{
            recoverPassword()
        }

        btnBack.setOnClickListener{
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun fetchUserData() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)
        val token = sharedPreferences.getString("accessToken", null)

        if (username != null && token != null) {
            userViewModel.fetchUserData(username, token)
            userViewModel.user.observe(this) { result ->
                result.onSuccess { userResponse ->
                    userId = userResponse.id
                    userUsername = userResponse.username
                    userFullname = userResponse.fullName
                    userEmail = userResponse.email
                    userGender = userResponse.gender
                    userNationality = userResponse.nationality

                    if(userGender == "Male"){gender = 0}
                    else if(userGender == "Female"){gender = 1}
                    else if(userGender == "Other"){gender = 2}
                    else if(userGender == "PNS"){gender = 3}

                    findViewById<TextInputEditText>(R.id.txtUsername).setText(userUsername)
                    findViewById<TextInputEditText>(R.id.txtFullname).setText(userFullname)
                    findViewById<TextInputEditText>(R.id.txtEmail).setText(userEmail)
                    findViewById<Spinner>(R.id.txtGender).setSelection(gender)
                    findViewById<AutoCompleteTextView>(R.id.txtNationality).setText(userNationality)

                    loadingProgressBar.visibility = View.GONE
                    overlayView.visibility = View.GONE
                }.onFailure {
                    showSnackBar("Algo de errado ocorreu!","FECHAR", R.color.red)
                    Log.e("HomeActivity", "Failed to fetch user data", it)
                }
            }
        } else {
            showToast("Invalid Username or Token")
            Log.e("HomeActivity", "Invalid username or token")
        }
    }

    private fun deleteUserData() {
        userViewModel.deleteUserData(userId)
    }

    private fun updateUserData(){
        userGender = when (userGender) {
            "Masculino" -> "Male"
            "Feminino" -> "Female"
            "Outro" -> "Other"
            "Prefiro não dizer" -> "PNS"
            else -> userGender
        }
        userViewModel.updateUserData(userId.toString(), token, userUsername, userFullname, userEmail, userGender, userNationality)
    }

    private fun recoverPassword() {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.dialog_reset_password, null)
        val etNewPassword = view.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmNewPassword = view.findViewById<TextInputEditText>(R.id.etConfirmNewPassword)

        val builder = AlertDialog.Builder(this)
        builder.setView(view)
            .setTitle("Alterar minha senha")
            .setPositiveButton("Confirmar") { dialog, _ ->
                val newPassword = etNewPassword.text.toString()
                val confirmNewPassword = etConfirmNewPassword.text.toString()

                if (newPassword == confirmNewPassword) {
                    val sharedPreferences: SharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
                    val username = sharedPreferences.getString("username", null)

                    if (username != null && token != null) {
                        userViewModel.recoverPassword(username, newPassword, token!!)
                        showToast("Password reset successfully")
                    } else {
                        showToast("Invalid Username or Token")
                    }
                } else {
                    showToast("Passwords do not match")
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun toggleEditMode(editText: View, button: ImageButton, isEditing: Boolean) {
        if (isEditing) {
            editText.isEnabled = false
            button.setImageResource(R.drawable.baseline_edit_24)
        } else {
            editText.isEnabled = true
            editText.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            button.setImageResource(R.drawable.baseline_check_24)
        }
    }

    private fun showSnackBar(message: String, action: String, bgTint: Int){
        val rootView = findViewById<View>(android.R.id.content)
        val snackBar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT)
            .setAction(action) {}
        snackBar.setBackgroundTint(ContextCompat.getColor(this, bgTint))
        snackBar.setTextColor(ContextCompat.getColor(this, R.color.white))
        snackBar.setActionTextColor(ContextCompat.getColor(this, R.color.white))
        snackBar.show()
    }

    private fun loadNationalities(): List<Nationality> {
        val jsonFileString = getNationality("nationalities.json")
        val gson = Gson()
        val listNationalityType = object : TypeToken<List<Nationality>>() {}.type
        return gson.fromJson(jsonFileString, listNationalityType)
    }
    private fun getNationality(fileName: String): String? {
        return try {
            val inputStream = assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }
}
