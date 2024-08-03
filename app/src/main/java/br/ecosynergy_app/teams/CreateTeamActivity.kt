package br.ecosynergy_app.teams

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import br.ecosynergy_app.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import de.hdodenhof.circleimageview.CircleImageView
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.RetrofitClient

class CreateTeamActivity : AppCompatActivity() {

    private lateinit var teamsViewModel: TeamsViewModel

    private lateinit var btnBack: ImageButton
    private lateinit var shimmerImg: ShimmerFrameLayout
    private lateinit var imgTeam: CircleImageView
    private lateinit var txtHandle: TextInputEditText
    private lateinit var txtTeamName: TextInputEditText
    private lateinit var txtDescription: TextInputEditText
    private lateinit var txtSector: TextInputEditText
    private lateinit var txtPlan: TextInputEditText
    private lateinit var btnCreateTeam: Button

    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var overlayView: View

    private var token: String? = ""
    private var userId: Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_team)

        val sp: SharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        token = sp.getString("accessToken", null)
        userId = sp.getString("id", null)?.toInt()

        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        overlayView = findViewById(R.id.overlayView)

        btnBack = findViewById(R.id.btnBack)

        teamsViewModel = ViewModelProvider(this, TeamsViewModelFactory(RetrofitClient.teamsService))[TeamsViewModel::class.java]

        shimmerImg = findViewById(R.id.shimmerImg)
        imgTeam = findViewById(R.id.imgTeam)
        txtHandle = findViewById(R.id.txtHandle)
        txtTeamName = findViewById(R.id.txtTeamName)
        txtDescription = findViewById(R.id.txtDescription)
        txtSector = findViewById(R.id.txtSector)
        txtPlan = findViewById(R.id.txtPlan)
        btnCreateTeam = findViewById(R.id.btnCreateTeam)

        btnBack.setOnClickListener{ finish() }

        btnCreateTeam.setOnClickListener{
            createTeam()
        }

        txtTeamName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val firstLetter = s?.toString()?.firstOrNull()
                if (firstLetter != null) {
                    shimmerImg.animate().alpha(0f).setDuration(300).withEndAction {
                        shimmerImg.stopShimmer()
                        shimmerImg.animate().alpha(1f).setDuration(300)
                        shimmerImg.visibility = View.GONE
                        imgTeam.visibility = View.VISIBLE
                    }
                    val drawableId = getDrawableForLetter(firstLetter)
                    imgTeam.setImageResource(drawableId)
                } else {
                    shimmerImg.startShimmer()
                    shimmerImg.visibility = View.VISIBLE
                    imgTeam.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                shimmerImg.startShimmer()
                shimmerImg.visibility = View.VISIBLE
                imgTeam.visibility = View.GONE
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                shimmerImg.startShimmer()
                shimmerImg.visibility = View.VISIBLE
                imgTeam.visibility = View.GONE
            }
        })


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

    private fun createTeam(){
        loadingProgressBar.visibility = View.VISIBLE
        overlayView.visibility = View.VISIBLE
        val handle: String = txtHandle.text.toString()
        val name: String = txtTeamName.text.toString()
        val description: String = txtDescription.text.toString()
        val members: List<Int?> = listOf(userId)
        val teamsRequest = TeamsRequest(handle,name,description,members)
        teamsViewModel.createTeam(token, teamsRequest)

        finish()
    }

}
