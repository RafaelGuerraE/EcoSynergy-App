package br.ecosynergy_app.home

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import br.ecosynergy_app.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class UserSettingsActivity : AppCompatActivity() {

    private val userViewModel: UserViewModel by viewModels()
    private var userId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usersettings)

        val rootView = findViewById<View>(android.R.id.content)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnEditUsername = findViewById<ImageButton>(R.id.btnEditUsername)
        val btnEditFullname = findViewById<ImageButton>(R.id.btnEditFullname)
        val btnEditGender = findViewById<ImageButton>(R.id.btnEditGender)
        val btnEditNationality = findViewById<ImageButton>(R.id.btnEditNationality)
        val btnEditPassword = findViewById<ImageButton>(R.id.btnEditPassword)
        val txtUsername = findViewById<TextInputEditText>(R.id.txtUsername)
        val txtFullname = findViewById<TextInputEditText>(R.id.txtFullname)
        val txtGender = findViewById<TextInputEditText>(R.id.txtGender)
        val txtNationality = findViewById<TextInputEditText>(R.id.txtNationality)
        val txtPassword = findViewById<TextInputEditText>(R.id.txtPassword)
        val btnDelete = findViewById<Button>(R.id.btnDelete)

        fetchUserData()

        btnEditUsername.setOnClickListener{
            val snackbar = Snackbar.make(rootView, "Você não pode alterar seu Nome de Usuário", Snackbar.LENGTH_LONG)
                .setAction("FECHAR") {}
            snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.red))
            snackbar.setTextColor(ContextCompat.getColor(this, R.color.white))
            snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.white))
            snackbar.show()
        }

        btnEditFullname.setOnClickListener{
            txtFullname.isEnabled = true
            txtFullname.text?.let { it1 -> txtFullname.setSelection(it1.length) }
            val imm = txtFullname.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(txtFullname, InputMethodManager.SHOW_IMPLICIT)
            btnEditFullname.setImageResource(R.drawable.baseline_check_24)
        }

        btnEditGender.setOnClickListener{
            txtGender.isEnabled = true
            txtGender.text?.let { it1 -> txtFullname.setSelection(it1.length) }
            val imm = txtGender.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(txtGender, InputMethodManager.SHOW_IMPLICIT)
            btnEditGender.setImageResource(R.drawable.baseline_check_24)
        }

        btnEditNationality.setOnClickListener{
            txtNationality.isEnabled = true
            txtNationality.text?.let { it1 -> txtNationality.setSelection(it1.length) }
            val imm = txtNationality.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(txtNationality, InputMethodManager.SHOW_IMPLICIT)
            btnEditNationality.setImageResource(R.drawable.baseline_check_24)
        }

        btnEditPassword.setOnClickListener{
            txtPassword.isEnabled = true
            txtPassword.text?.let { it1 -> txtPassword.setSelection(it1.length) }
            val imm = txtPassword.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(txtPassword, InputMethodManager.SHOW_IMPLICIT)
            btnEditPassword.setImageResource(R.drawable.baseline_check_24)
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

        btnBack.setOnClickListener(){
            finish()
        }
    }

    fun showToast(message: String) {
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
                    findViewById<TextInputEditText>(R.id.txtUsername).setText(userResponse.username)
                    findViewById<TextInputEditText>(R.id.txtFullname).setText(userResponse.fullName)
                    findViewById<TextInputEditText>(R.id.txtGender).setText(userResponse.gender)
                    findViewById<TextInputEditText>(R.id.txtNationality).setText(userResponse.nationality)
                    findViewById<TextInputEditText>(R.id.txtPassword).setText(userResponse.password)
                }.onFailure {
                    showToast("Failed to fetchUserData")
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
}
