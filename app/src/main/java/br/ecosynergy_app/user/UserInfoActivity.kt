package br.ecosynergy_app.user;

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle;
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity;
import br.ecosynergy_app.R
import br.ecosynergy_app.home.HomeActivity
import br.ecosynergy_app.signup.Nationality
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class UserInfoActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var txtFullname: TextView
    private lateinit var txtUsername: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtGender: TextView
    private lateinit var txtNationality: TextView
    private lateinit var txtCreation: TextView
    private lateinit var imgUser: CircleImageView

    override fun onCreate (savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        btnBack = findViewById(R.id.btnBack)
        txtFullname = findViewById(R.id.txtFullname)
        txtUsername = findViewById(R.id.txtUsername)
        txtEmail = findViewById(R.id.txtEmail)
        txtGender = findViewById(R.id.txtGender)
        txtNationality = findViewById(R.id.txtNationality)
        txtCreation = findViewById(R.id.txtCreation)
        imgUser = findViewById(R.id.imgUser)

        btnBack.setOnClickListener { finish() }

        val username = intent.getStringExtra("USERNAME") ?: ""
        val fullname = intent.getStringExtra("FULLNAME") ?: ""
        val email = intent.getStringExtra("EMAIL") ?: ""
        val nationality = intent.getStringExtra("NATIONALITY") ?: ""
        var created = intent.getStringExtra("CREATED") ?: ""

        created = formatDateToMonthYear(created)

        val nationalities = loadNationalities()

        val nationalityReverseMap = nationalities.associate { it.nationality to it.nationality_br }

        val gender = when (intent.getStringExtra("GENDER") ?: "") {
            "Male" -> "Masculino"
            "Female" -> "Feminino"
            "Other" -> "Outro"
            "PNS" -> "Prefiro não dizer"
            else -> ""
        }

        txtFullname.text = fullname
        txtUsername.text = "@$username"
        txtEmail.text = email
        txtGender.text = gender
        txtNationality.text = nationalityReverseMap[nationality]
        txtCreation.text = "Conta criada em $created"

        imgUser.setImageResource(HomeActivity().getDrawableForLetter(fullname.first()))

        setCopyOnLongClick(txtUsername, "Nome de usuário")
        setCopyOnLongClick(txtEmail, "Email")
    }

    private fun setCopyOnLongClick(textView: TextView, field: String) {
        textView.setOnLongClickListener {
            val textToCopy = textView.text.toString()
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("$field copiado",textToCopy)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "$field copiado", Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun formatDateToMonthYear(dateString: String): String {
        val zonedDateTime = ZonedDateTime.parse(dateString)

        val formatter = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale("pt", "BR"))

        return zonedDateTime.format(formatter)
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