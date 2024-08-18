package br.ecosynergy_app.teams

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import br.ecosynergy_app.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.textfield.TextInputEditText
import de.hdodenhof.circleimageview.CircleImageView
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.RetrofitClient
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Spinner
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CreateTeamBottomSheet : BottomSheetDialogFragment() {

    private lateinit var teamsViewModel: TeamsViewModel

    private lateinit var btnClose: ImageButton
    private lateinit var shimmerImg: ShimmerFrameLayout
    private lateinit var imgTeam: CircleImageView
    private lateinit var txtHandle: TextInputEditText
    private lateinit var txtTeamName: TextInputEditText
    private lateinit var txtDescription: TextInputEditText
    private lateinit var spinnerActivities: Spinner
    private lateinit var txtPlan: TextInputEditText
    private lateinit var btnCreateTeam: Button

    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var overlayView: View

    private var token: String? = ""
    private var userId: Int? = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_team_bottom_sheet, container, false)

        val sp: SharedPreferences = requireActivity().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        token = sp.getString("accessToken", null)
        userId = sp.getString("id", null)?.toInt()

        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)
        overlayView = view.findViewById(R.id.overlayView)

        btnClose = view.findViewById(R.id.btnClose)

        teamsViewModel = ViewModelProvider(requireActivity(), TeamsViewModelFactory(RetrofitClient.teamsService))[TeamsViewModel::class.java]

        shimmerImg = view.findViewById(R.id.shimmerImg)
        imgTeam = view.findViewById(R.id.imgTeam)
        txtHandle = view.findViewById(R.id.txtHandle)
        txtTeamName = view.findViewById(R.id.txtTeamName)
        txtDescription = view.findViewById(R.id.txtDescription)
        spinnerActivities = view.findViewById(R.id.spinnerActivities)
        txtPlan = view.findViewById(R.id.txtPlan)
        btnCreateTeam = view.findViewById(R.id.btnCreateTeam)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.peekHeight = 2000
        }

        spinnerActivities.isEnabled = false

        btnClose.setOnClickListener{ dismiss() }

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

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                shimmerImg.startShimmer()
                shimmerImg.visibility = View.VISIBLE
                imgTeam.visibility = View.GONE
            }
        })
    }

    private fun createTeam(){
        loadingProgressBar.visibility = View.VISIBLE
        overlayView.visibility = View.VISIBLE
        val handle: String = txtHandle.text.toString()
        val name: String = txtTeamName.text.toString()
        val description: String = txtDescription.text.toString()
        val members: List<Member> = listOf(Member(userId, "ADMINISTRATOR"))
        val teamsRequest = TeamsRequest(handle,name,description, ActivitiesRequest(1), "timeZone", members)
        teamsViewModel.createTeam(token, teamsRequest)

        dismiss()
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
}

