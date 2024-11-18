package br.ecosynergy_app.user

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageButton
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
import br.ecosynergy_app.signup.viewmodel.SignUpViewModel
import br.ecosynergy_app.signup.viewmodel.SignUpViewModelFactory
import br.ecosynergy_app.user.viewmodel.UserViewModel
import br.ecosynergy_app.user.viewmodel.UserViewModelFactory
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

class UserSettingsActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var signUpViewModel: SignUpViewModel

    private var userId: Int = 0
    private var userUsername: String = ""
    private var userFullname: String = ""
    private var userEmail: String = ""
    private var userNationality: String = ""
    private var userGender: String = ""
    private var accessToken: String = ""
    private var refreshToken: String = ""
    private var gender: Int = 0

    private var verificationCode: String = ""

    private var lastUsername: String = ""
    private var lastFullname: String = ""
    private var lastEmail: String = ""
    private var lastGender: Int = 0
    private var lastNationality: String = ""

    private var nationality: String = ""

    private var nationalityMap: Map<String?, String> = mapOf()

    private lateinit var btnBack: ImageButton
    private lateinit var txtUsername: EditText
    private lateinit var txtFullname: EditText
    private lateinit var txtEmail: EditText
    private lateinit var txtGender: Spinner
    private lateinit var txtNationality: AutoCompleteTextView
    private lateinit var btnDelete: MaterialButton

    private lateinit var shimmerEffect: ShimmerFrameLayout
    private lateinit var imgProfile: CircleImageView

    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var overlayView: View

    private lateinit var loginSp: SharedPreferences

    private lateinit var btnEditContact: TextView
    private lateinit var btnEditEmail: TextView
    private lateinit var btnEditPersonal: TextView

    private lateinit var btnCancelContact: TextView
    private lateinit var btnCancelEmail: TextView
    private lateinit var btnCancelPersonal: TextView

    private var isEditingContact: Boolean = false
    private var isEditingEmail: Boolean = false
    private var isEditingPersonal: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_settings)

        val userRepository = UserRepository(AppDatabase.getDatabase(this).userDao())

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(RetrofitClient.userService, userRepository)
        )[UserViewModel::class.java]
        signUpViewModel = ViewModelProvider(
            this,
            SignUpViewModelFactory(RetrofitClient.signUpService)
        )[SignUpViewModel::class.java]

        loginSp = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

        btnBack = findViewById(R.id.btnBack)
        txtUsername = findViewById(R.id.txtUsername)
        txtFullname = findViewById(R.id.txtFullname)
        txtEmail = findViewById(R.id.txtEmail)
        txtGender = findViewById(R.id.txtGender)
        txtNationality = findViewById(R.id.txtNationality)
        btnDelete = findViewById(R.id.btnDelete)

        shimmerEffect = findViewById(R.id.shimmerImage)
        imgProfile = findViewById(R.id.imgProfile)

        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        overlayView = findViewById(R.id.overlayView)

        btnEditContact = findViewById(R.id.btnEditContact)
        btnEditEmail = findViewById(R.id.btnEditEmail)
        btnEditPersonal = findViewById(R.id.btnEditPersonal)

        btnCancelContact = findViewById(R.id.btnCancelContact)
        btnCancelEmail = findViewById(R.id.btnCancelEmail)
        btnCancelPersonal = findViewById(R.id.btnCancelPersonal)

        txtGender.isEnabled = false


        disableContact()
        disableEmail()
        disablePersonal()

        val nationalities = loadNationalities()
        nationalityMap = nationalities.associate { it.nationality_br to it.nationality }
        val nationalityBr = nationalities.mapNotNull { it.nationality_br }
        val nationalityAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nationalityBr)
        txtNationality.setAdapter(nationalityAdapter)

        txtNationality.setOnItemClickListener { parent, _, position, _ ->
            val selectedNationalityBr = parent.getItemAtPosition(position) as String
            nationality = nationalityMap[selectedNationalityBr] ?: "Unknown"
        }

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

        btnEditContact.setOnClickListener {
            if (isEditingEmail || isEditingPersonal) {
                showToast("Você ja está editando outros campos")
            } else {
                if (!isEditingContact) {
                    enableContact()
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Você deseja alterar suas informações?")
                    builder.setMessage("Se você alterar o seu nome de usuário, você será obrigado a fazer login novamente.")

                    builder.setPositiveButton("Sim") { dialog, _ ->
                        userUsername = txtUsername.text.toString()
                        userFullname = txtFullname.text.toString()
                        userEmail = txtEmail.text.toString()
                        userGender = txtGender.selectedItem.toString()
                        userNationality = nationality

                        updateUserData {

                            dialog.dismiss()

                            if (lastUsername != userUsername) {
                                val resultIntent = Intent().apply {
                                    putExtra("USERNAME_CHANGED", true)
                                }
                                setResult(RESULT_OK, resultIntent)
                                finish()
                            }
                        }
                    }

                    builder.setNegativeButton("Cancelar") { dialog, _ ->
                        dialog.dismiss()
                    }

                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }
            }
        }

        btnEditEmail.setOnClickListener {
            if (isEditingContact || isEditingPersonal) {
                showToast("Você ja está editando outros campos")
            } else {
                if (!isEditingEmail) {
                    enableEmail()
                } else {
                    if (txtEmail.text.toString() == lastEmail) {
                        showToast("Este é o mesmo E-mail cadastrado")
                    } else {
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Você deseja alterar seu email?")
                        builder.setMessage("Para alterá-lo você será redirecionado a uma tela de confirmação para o novo E-mail.")

                        builder.setPositiveButton("Sim") { dialog, _ ->
                            confirmationCode(
                                txtEmail.text.toString(),
                                txtFullname.text.toString()
                            ) {

                                val dialogView =
                                    layoutInflater.inflate(R.layout.dialog_verification, null)
                                val txtCode = dialogView.findViewById<EditText>(R.id.txtCode)

                                txtCode.addTextChangedListener(object : TextWatcher {
                                    override fun afterTextChanged(s: Editable?) {
                                        val uppercaseText = s.toString().uppercase()

                                        if (s.toString() != uppercaseText) {
                                            txtCode.setText(uppercaseText)
                                            txtCode.setSelection(uppercaseText.length)
                                        }
                                    }

                                    override fun beforeTextChanged(
                                        s: CharSequence?,
                                        start: Int,
                                        count: Int,
                                        after: Int
                                    ) {
                                    }

                                    override fun onTextChanged(
                                        s: CharSequence?,
                                        start: Int,
                                        before: Int,
                                        count: Int
                                    ) {
                                    }
                                })

                                val alertDialog = AlertDialog.Builder(this)
                                    .setTitle("Código de Verificação")
                                    .setView(dialogView)
                                    .setPositiveButton("Confirmar") { dialog, _ ->
                                        if (txtCode.text.toString() == verificationCode) {
                                            userUsername = txtUsername.text.toString()
                                            userFullname = txtFullname.text.toString()
                                            userEmail = txtEmail.text.toString()
                                            userGender = txtGender.selectedItem.toString()
                                            userNationality = nationality

                                            updateUserData {
                                                disableEmail()

                                                dialog.dismiss()
                                            }

                                        } else {
                                            showToast("Erro: Código Incorreto")
                                        }
                                    }
                                    .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
                                    .show()

                                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.green))
                                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.red))

                                dialog.dismiss()
                            }
                        }
                        builder.setNegativeButton("Cancelar") { dialog, _ ->
                            dialog.dismiss()
                        }

                        val dialog: AlertDialog = builder.create()
                        dialog.show()
                    }
                }
            }
        }


        btnEditPersonal.setOnClickListener {
            if (isEditingContact || isEditingEmail) {
                showToast("Você ja está editando outros campos")
            } else {
                if (!isEditingPersonal) {
                    enablePersonal()
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Você deseja alterar suas informações?")
                    builder.setMessage("Suas informações de gênero e nacionalidade serão atualizados.")

                    builder.setPositiveButton("Sim") { dialog, _ ->
                        userUsername = txtUsername.text.toString()
                        userFullname = txtFullname.text.toString()
                        userEmail = txtEmail.text.toString()
                        userGender = txtGender.selectedItem.toString()
                        userNationality = nationality

                        updateUserData {
                            dialog.dismiss()
                        }
                    }

                    builder.setNegativeButton("Cancelar") { dialog, _ ->
                        dialog.dismiss()
                    }

                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }
            }
        }


        btnCancelEmail.setOnClickListener {
            txtEmail.setText(lastEmail)
            disableEmail()
        }
        btnCancelPersonal.setOnClickListener {
            txtNationality.setText(lastNationality)
            txtGender.setSelection(lastGender)
            disablePersonal()
        }
        btnCancelContact.setOnClickListener {
            txtUsername.setText(lastUsername)
            txtFullname.setText(lastFullname)
            disableContact()
        }

        btnDelete.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirme sua Ação")
            builder.setMessage("Você deseja excluir sua conta?")

            builder.setPositiveButton("Sim") { _, _ ->
                deleteUserData()
                val resultIntent = Intent().apply {
                    putExtra("USER_DELETED", true)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }

            builder.setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun confirmationCode(userEmail: String, userFullname: String, onComplete: () -> Unit) {
        signUpViewModel.confirmationCode(userEmail, userFullname) {
            val code = signUpViewModel.verificationCode.value
            if (code != null) {
                verificationCode = code
                Log.d("UserSettingsActivity", "Confirmation code: $code")
            }
            onComplete()
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun disableContact() {
        btnEditContact.text = "Editar"
        isEditingContact = false
        btnCancelContact.visibility = View.GONE

        txtFullname.isEnabled = false
        txtUsername.isEnabled = false
        txtFullname.setTextColor(ContextCompat.getColor(this, R.color.disabled))
        txtUsername.setTextColor(ContextCompat.getColor(this, R.color.disabled))
    }

    private fun enableContact() {
        btnCancelContact.visibility = View.VISIBLE
        isEditingContact = true
        btnEditContact.text = "Confirmar"

        txtFullname.isEnabled = true
        txtUsername.isEnabled = true
        txtFullname.setTextColor(getThemeColor(android.R.attr.textColorPrimary))
        txtUsername.setTextColor(getThemeColor(android.R.attr.textColorPrimary))
    }

    private fun disableEmail() {
        btnEditEmail.text = "Alterar"
        isEditingEmail = false
        btnCancelEmail.visibility = View.GONE

        txtEmail.isEnabled = false
        txtEmail.setTextColor(ContextCompat.getColor(this, R.color.disabled))
    }

    private fun enableEmail() {
        isEditingEmail = true
        btnEditEmail.text = "Confirmar"
        btnCancelEmail.visibility = View.VISIBLE
        txtEmail.isEnabled = true
        txtEmail.setTextColor(getThemeColor(android.R.attr.textColorPrimary))
    }

    private fun disablePersonal() {
        isEditingPersonal = false
        btnEditPersonal.text = "Editar"
        btnCancelPersonal.visibility = View.GONE
        txtGender.isEnabled = false
        txtNationality.isEnabled = false
        txtNationality.setTextColor(ContextCompat.getColor(this, R.color.disabled))
    }

    private fun enablePersonal() {
        isEditingPersonal = true
        btnEditPersonal.text = "Confirmar"
        btnCancelPersonal.visibility = View.VISIBLE
        txtGender.isEnabled = true
        txtNationality.isEnabled = true
        txtNationality.setTextColor(getThemeColor(android.R.attr.textColorPrimary))
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

                nationality = userInfo.nationality

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



                lastUsername = userUsername
                lastFullname = userFullname
                lastEmail = userEmail
                lastGender = gender
                lastNationality = userNationality

                loadingProgressBar.visibility = View.GONE
                overlayView.visibility = View.GONE
            }
        }
    }

    private fun deleteUserData(){
        userViewModel.deleteUserData(userId, accessToken)
    }

    private fun updateUserData(onComplete: () -> Unit) {
        userGender = when (userGender) {
            "Masculino" -> "Male"
            "Feminino" -> "Female"
            "Outro" -> "Other"
            "Prefiro não dizer" -> "PNS"
            else -> userGender
        }
        userViewModel.updateUserData(
            userId,
            accessToken,
            refreshToken,
            userUsername,
            userFullname,
            userEmail,
            userGender,
            userNationality
        ) {
            val result = userViewModel.update.value

            if (result != null) {
                if (userEmail != lastEmail) {
                    if (result.isSuccessful) {
                        showToast("E-mail alterado com sucesso")
                        disableEmail()
                    } else {
                        showToast("Este email já está em uso.")
                        disableEmail()
                        txtEmail.setText(lastEmail)
                    }
                } else {
                    if (result.isSuccessful) {
                        showToast("Informações editadas com sucesso!")
                        disablePersonal()
                        disableEmail()
                        disableContact()
                    } else {
                        showToast("Erro: Verifique as informações inseridas")
                        disablePersonal()
                        disableEmail()
                        disableContact()

                        txtUsername.setText(lastUsername)
                        txtFullname.setText(lastFullname)
                        txtNationality.setText(lastNationality)
                        txtGender.setSelection(lastGender)
                        txtEmail.setText(lastEmail)
                    }
                }
            }
        }


        onComplete()
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
                    showToast("ERRO: Imagem de Perfil")
                }
            } else {
                Log.e("UserSettingsActivity", "Full name is empty")
                showToast("ERRO: Imagem de Perfil")
            }

            shimmerEffect.animate().alpha(0f).setDuration(300).withEndAction {
                shimmerEffect.stopShimmer()
                shimmerEffect.animate().alpha(1f).setDuration(300)
                shimmerEffect.visibility = View.GONE
                imgProfile.visibility = View.VISIBLE
            }
        }
    }
}