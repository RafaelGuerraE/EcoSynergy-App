package br.ecosynergy_app.teams

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import br.ecosynergy_app.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.hdodenhof.circleimageview.CircleImageView

class TeamBottomSheet : BottomSheetDialogFragment() {

    private lateinit var btnClose : ImageButton
    private lateinit var lblTeamName: TextView
    private lateinit var imgTeam: CircleImageView

    private var teamHandle: String? = null
    private var teamInitial: Int = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        teamHandle = arguments?.getString("TEAM_HANDLE")
        teamInitial = arguments?.getInt("TEAM_INITIAL") ?: 0

        return inflater.inflate(R.layout.fragment_team_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.peekHeight = 2000
        }

        btnClose = view.findViewById(R.id.btnClose)
        lblTeamName = view.findViewById(R.id.lblTeamName)
        imgTeam = view.findViewById(R.id.imgTeam)

        lblTeamName.text = "@$teamHandle"
        imgTeam.setImageResource(teamInitial)

        btnClose.setOnClickListener{
            dismiss()
        }
    }
}