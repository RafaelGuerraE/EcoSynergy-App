package br.ecosynergy_app.home

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.register.Nationality
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

class UserSettingsActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel
    private var gender: Int = 0

    private var userId: String = ""
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

        userViewModel = ViewModelProvider(this, UserViewModelFactory(RetrofitClient.userService))[UserViewModel::class.java]

        val sharedPreferences: SharedPreferences =
            getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("accessToken", null)

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
        val adapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nationalityNames)
        txtNationality.setAdapter(adapter)

        txtGender.isEnabled = false

        txtNationality.setTextColor(ContextCompat.getColor(this, R.color.disabled))

        loadingProgressBar.visibility = View.VISIBLE
        overlayView.visibility = View.VISIBLE

        fetchUserData()
        updateProfileImage()

        txtUsername.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val lowercaseText = s.toString().lowercase()

                if (s.toString() != lowercaseText) {
                    txtUsername.setText(lowercaseText)
                    txtUsername.setSelection(lowercaseText.length)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        fun setEditButtons(button: ImageButton) {
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

        btnEditUsername.setOnClickListener {
            toggleEditMode(txtUsername, btnEditUsername, isEditingUsername)
            setEditButtons(btnEditUsername)
            isEditingUsername = !isEditingUsername
            if (!isEditingUsername) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Você deseja alterar seu Username?")
                builder.setMessage("Para alterar seu Username, você deve fazer o login novamente")

                builder.setPositiveButton("Sim") { dialog, _ ->
                    userUsername = txtUsername.text.toString()
                    updateUserData()
                    enableAllButtons()
                    showSnackBar("Nome de usuário alterado com sucesso", "FECHAR", R.color.greenDark)
                    dialog.dismiss()
                    finish()
                }

                builder.setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }

                val dialog: AlertDialog = builder.create()
                dialog.show()

            }
        }

        btnEditFullname.setOnClickListener {
            toggleEditMode(txtFullname, btnEditFullname, isEditingFullname)
            setEditButtons(btnEditFullname)
            isEditingFullname = !isEditingFullname
            if (!isEditingFullname) {
                userFullname = txtFullname.text.toString()
                updateUserData()
                enableAllButtons()
                showSnackBar("Nome completo alterado com sucesso", "FECHAR", R.color.greenDark)
            }
        }

        btnEditEmail.setOnClickListener {
            toggleEditMode(txtEmail, btnEditEmail, isEditingEmail)
            setEditButtons(btnEditEmail)
            isEditingEmail = !isEditingEmail
            if (!isEditingEmail) {
                userEmail = txtEmail.text.toString()
                updateUserData()
                enableAllButtons()
                showSnackBar("Email alterado com sucesso", "FECHAR", R.color.greenDark)
            }
        }

        btnEditGender.setOnClickListener {
            toggleEditMode(txtGender, btnEditGender, isEditingGender)
            setEditButtons(btnEditGender)
            isEditingGender = !isEditingGender
            if (!isEditingGender) {
                userGender = txtGender.selectedItem.toString()
                updateUserData()
                enableAllButtons()
                showSnackBar("Genêro alterado com sucesso", "FECHAR", R.color.greenDark)
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

        btnEditNationality.setOnClickListener {
            toggleEditMode(txtNationality, btnEditNationality, isEditingNationality)
            setEditButtons(btnEditNationality)
            setTextColorPrimary(txtNationality)
            isEditingNationality = !isEditingNationality
            if (!isEditingNationality) {
                userNationality = txtNationality.text.toString()
                updateUserData()
                enableAllButtons()
                showSnackBar("Nacionalidade alterada com sucesso", "FECHAR", R.color.greenDark)
                txtNationality.setTextColor(ContextCompat.getColor(this, R.color.disabled))
            }
        }

        btnDelete.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirme sua Ação")
            builder.setMessage("Você deseja excluir sua conta?")

            builder.setPositiveButton("Sim") { dialog, _ ->
                deleteUserData()
                dialog.dismiss()
                finish()
            }

            builder.setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        btnPassword.setOnClickListener {
            resetPassword()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun fetchUserData() {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val identifier = sharedPreferences.getString("identifier", null)
        val token = sharedPreferences.getString("accessToken", null)

        if (identifier != null && token != null) {
            userViewModel.fetchUserData(identifier, token)
            userViewModel.user.observe(this) { result ->
                result.onSuccess { userResponse ->
                    userId = userResponse.id.toString()
                    userUsername = userResponse.username
                    userFullname = userResponse.fullName
                    userEmail = userResponse.email
                    userGender = userResponse.gender
                    userNationality = userResponse.nationality

                    if (userGender == "Male") {
                        gender = 0
                    } else if (userGender == "Female") {
                        gender = 1
                    } else if (userGender == "Other") {
                        gender = 2
                    } else if (userGender == "PNS") {
                        gender = 3
                    }

                    findViewById<TextInputEditText>(R.id.txtUsername).setText(userUsername)
                    findViewById<TextInputEditText>(R.id.txtFullname).setText(userFullname)
                    findViewById<TextInputEditText>(R.id.txtEmail).setText(userEmail)
                    findViewById<Spinner>(R.id.txtGender).setSelection(gender)
                    findViewById<AutoCompleteTextView>(R.id.txtNationality).setText(userNationality)

                    loadingProgressBar.visibility = View.GONE
                    overlayView.visibility = View.GONE
                }.onFailure {
                    showSnackBar("Algo de errado ocorreu!", "FECHAR", R.color.red)
                    Log.e("HomeActivity", "Failed to fetch user data", it)
                }
            }
        } else {
            showToast("Invalid Username or Token")
            Log.e("HomeActivity", "Invalid username or token")
        }
    }

    private fun deleteUserData() {
        token = getSharedPreferences("login_prefs", Context.MODE_PRIVATE).getString("accessToken", null)
        userViewModel.deleteUserData(userId, token)
    }

    private fun updateUserData() {
        userGender = when (userGender) {
            "Masculino" -> "Male"
            "Feminino" -> "Female"
            "Outro" -> "Other"
            "Prefiro não dizer" -> "PNS"
            else -> userGender
        }
        userViewModel.updateUserData(
            userId,
            token,
            userUsername,
            userFullname,
            userEmail,
            userGender,
            userNationality
        )
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
                    showSnackBar("Erro: os campos não podem estar vazios", "FECHAR", R.color.red)
                } else if (newPassword != confirmNewPassword) {
                    showSnackBar("Erro: as senhas devem se corresponder", "FECHAR", R.color.red)
                } else {
                    val sharedPreferences: SharedPreferences =
                        getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
                    val username = sharedPreferences.getString("username", null)

                    if (username != null && token != null) {
                        dialog.dismiss()
                        userViewModel.resetPassword(username, newPassword, token!!)
                        showSnackBar("Senha alterada com sucesso", "FECHAR", R.color.greenDark)
                    } else {
                        showToast("Invalid Username or Token")
                    }
                }
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

    private fun showSnackBar(message: String, action: String, bgTint: Int) {
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

    private fun updateProfileImage() {
        val shimmerEffect = findViewById<ShimmerFrameLayout>(R.id.shimmerImage)
        val imgProfile: CircleImageView = findViewById(R.id.imgProfile)

        userViewModel.user.observe(this) { user ->
            user.onSuccess { userData ->
                shimmerEffect.visibility = View.VISIBLE
                imgProfile.visibility = View.GONE
                val fullName = userData.fullName

                if (fullName.isNotEmpty()) {
                    val firstName = fullName.split(" ").firstOrNull() ?: ""

                    if (firstName.isNotEmpty()) {
                        val firstLetter = firstName.first()
                        val drawableId = getDrawableForLetter(firstLetter)
                        imgProfile.setImageResource(drawableId)
                    } else {
                        Log.e("UserSettingsActivity", "First name is empty")
                        showSnackBar("ERRO: Imagem de Perfil", "FECHAR", R.color.red)
                    }
                } else {
                    Log.e("UserSettingsActivity", "Full name is empty")
                    showSnackBar("ERRO: Imagem de Perfil", "FECHAR", R.color.red)
                }

                shimmerEffect.animate().alpha(0f).setDuration(300).withEndAction {
                    shimmerEffect.stopShimmer()
                    shimmerEffect.animate().alpha(1f).setDuration(300)
                    shimmerEffect.visibility = View.GONE
                    imgProfile.visibility = View.VISIBLE
                }
            }.onFailure { throwable ->
                Log.e("HomeActivity", "Error updating navigation header", throwable)
                shimmerEffect.visibility = View.GONE
                imgProfile.visibility = View.VISIBLE
            }
        }
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
}