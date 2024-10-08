package br.ecosynergy_app.user

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
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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
import br.ecosynergy_app.home.HomeActivity
import br.ecosynergy_app.signup.Nationality
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.user.UserRepository
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

class UserSettingsActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel

    private var userId: Int = 0
    private var userUsername: String = ""
    private var userFullname: String = ""
    private var userEmail: String = ""
    private var userNationality: String = ""
    private var userGender: String = ""
    private var accessToken: String = ""
    private var refreshToken: String = ""
    private var gender: Int = 0

    private var nationalityMap: Map<String?, String> = mapOf()

    private lateinit var btnBack: ImageButton
    private lateinit var txtUsername: TextInputEditText
    private lateinit var txtFullname: TextInputEditText
    private lateinit var txtEmail: TextInputEditText
    private lateinit var txtGender: Spinner
    private lateinit var txtNationality: AutoCompleteTextView
    private lateinit var btnPassword: MaterialButton
    private lateinit var btnDelete: MaterialButton

    private lateinit var shimmerEffect: ShimmerFrameLayout
    private lateinit var imgProfile: CircleImageView

    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var overlayView: View

    private lateinit var loginSp : SharedPreferences

    private lateinit var  btnEdit : ImageButton

    private var isEditing: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usersettings)

        val userDao = AppDatabase.getDatabase(this).userDao()
        val userRepository = UserRepository(userDao)

        userViewModel = ViewModelProvider(this, UserViewModelFactory(RetrofitClient.userService, userRepository))[UserViewModel::class.java]

        loginSp = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

        btnBack = findViewById(R.id.btnBack)
        txtUsername = findViewById(R.id.txtUsername)
        txtFullname = findViewById(R.id.txtFullname)
        txtEmail = findViewById(R.id.txtEmail)
        txtGender = findViewById(R.id.txtGender)
        txtNationality = findViewById(R.id.txtNationality)
        btnPassword = findViewById(R.id.btnPassword)
        btnDelete = findViewById(R.id.btnDelete)

        shimmerEffect = findViewById(R.id.shimmerImage)
        imgProfile = findViewById(R.id.imgProfile)

        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        overlayView = findViewById(R.id.overlayView)

        btnEdit = findViewById(R.id.btnEdit)

        txtGender.isEnabled = false

        txtNationality.setTextColor(ContextCompat.getColor(this, R.color.disabled))

        disableEditTexts()

        val nationalities = loadNationalities()
        nationalityMap = nationalities.associate { it.nationality_br to it.nationality }

        val nationalityBr = nationalities.map { it.nationality_br }

        val nationalityAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nationalityBr)
        txtNationality.setAdapter(nationalityAdapter)

        btnEdit.visibility = View.VISIBLE
        btnEdit.isEnabled = true
        btnEdit.isClickable = true

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

        btnEdit.setOnClickListener {
            Log.d("UserSettingsActivity", "btnEdit clicked")
            if (!isEditing) {
                isEditing = true
                enableEditTexts()
                btnEdit.setImageResource(R.drawable.baseline_check_24)
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Você deseja alterar suas informações?")
                builder.setMessage("Se você alterar o seu nome de usuário, você será obrigado a fazer login novamente.")

                builder.setPositiveButton("Sim") { dialog, _ ->
                    val lastUsername = userUsername

                    userUsername = txtUsername.text.toString()
                    userFullname = txtFullname.text.toString()
                    userEmail = txtEmail.text.toString()
                    userGender = txtGender.selectedItem.toString()
                    userNationality = txtNationality.text.toString()

                    updateUserData()

                    btnEdit.setImageResource(R.drawable.baseline_edit_24)
                    disableEditTexts()
                    isEditing = false
                    dialog.dismiss()
                    if (lastUsername != txtUsername.text.toString()){
                        finish()
                    }
                }

                builder.setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }

                val dialog: AlertDialog = builder.create()
                dialog.show()
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

        btnDelete.setOnClickListener {
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

    private fun enableEditTexts(){
        txtGender.isEnabled = true
        txtNationality.isEnabled = true
        txtEmail.isEnabled = true
        txtFullname.isEnabled = true
        txtUsername.isEnabled = true

        txtNationality.setTextColor(getThemeColor(android.R.attr.textColorPrimary))
    }

    private fun disableEditTexts(){
        txtGender.isEnabled = false
        txtNationality.isEnabled = false
        txtEmail.isEnabled = false
        txtFullname.isEnabled = false
        txtUsername.isEnabled = false

        txtNationality.setTextColor(ContextCompat.getColor(this, R.color.disabled))
    }

    private fun getThemeColor(attrResId: Int): Int {
        val typedValue = TypedValue()
        val theme = this.theme
        theme.resolveAttribute(attrResId, typedValue, true)
        return typedValue.data
    }

    private fun fetchUserData() {
        val nationalities = loadNationalities()
        val nationalityReverseMap = nationalities.associate { it.nationality to it.nationality_br }

        userViewModel.getUserInfoFromDB {
            userViewModel.userInfo.observe(this) { userInfo ->
                userId = userInfo.id
                userUsername = userInfo.username
                userFullname = userInfo.fullName
                userEmail = userInfo.email
                userGender = userInfo.gender

                userNationality = nationalityReverseMap[userInfo.nationality].toString()

                accessToken = userInfo.accessToken
                refreshToken = userInfo.refreshToken

                gender = when (userGender) {
                    "Male" -> 0
                    "Female" -> 1
                    "Other" -> 2
                    "PNS" -> 3
                    else -> -1
                }

                txtUsername.setText(userUsername)
                txtFullname.setText(userFullname)
                txtEmail.setText(userEmail)
                txtGender.setSelection(gender)
                txtNationality.setText(userNationality)

                loadingProgressBar.visibility = View.GONE
                overlayView.visibility = View.GONE
            }
        }
    }

    private fun deleteUserData() {
        userViewModel.deleteUserData(userId, accessToken)
        finish()
    }

    private fun updateUserData() {
        userGender = when (userGender) {
            "Masculino" -> "Male"
            "Feminino" -> "Female"
            "Outro" -> "Other"
            "Prefiro não dizer" -> "PNS"
            else -> userGender
        }

        val nationalities = loadNationalities()
        val nationalityMap = nationalities.associate { it.nationality_br to it.nationality }

        userNationality = nationalityMap[userNationality].toString()

        userViewModel.updateUserData(
            userId,
            accessToken,
            refreshToken,
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
                    val username = userUsername
                    userViewModel.resetPassword(username, newPassword, accessToken!!)
                    showSnackBar("Senha alterada com sucesso", "FECHAR", R.color.greenDark)
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
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

    private fun updateProfileImage() {
        userViewModel.userInfo.observe(this) { userData ->
                shimmerEffect.visibility = View.VISIBLE
                imgProfile.visibility = View.GONE

                val fullName = userData.fullName

                if (fullName.isNotEmpty()) {
                    val firstName = fullName.split(" ").firstOrNull() ?: ""

                    if (firstName.isNotEmpty()) {
                        val firstLetter = firstName.first()
                        val drawableId = HomeActivity().getDrawableForLetter(firstLetter)
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