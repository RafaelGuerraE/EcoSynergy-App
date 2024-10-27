package br.ecosynergy_app.teams

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.invites.InvitesRepository
import br.ecosynergy_app.room.teams.MembersRepository
import br.ecosynergy_app.room.teams.TeamsRepository
import br.ecosynergy_app.teams.viewmodel.TeamsViewModel
import br.ecosynergy_app.teams.viewmodel.TeamsViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class EditTeamGoalsBottomSheet : BottomSheetDialogFragment() {

    private lateinit var teamsViewModel: TeamsViewModel

    private lateinit var btnClose: ImageButton
    private lateinit var txtDailyGoal: TextView
    private lateinit var txtWeeklyGoal: TextView
    private lateinit var txtMonthlyGoal: TextView
    private lateinit var txtAnnualGoal: TextView

    private lateinit var btnConfirm: MaterialButton
    private lateinit var btnConfirmContainer: FrameLayout
    private lateinit var progressBarConfirm: ProgressBar

    private lateinit var btnInfo: ImageButton
    private lateinit var btnEditAll: LinearLayout

    private lateinit var teamHandle: String
    private var teamId: Int = 0
    private var userId: Int = 0
    private lateinit var accessToken: String
    private var dailyGoal: Double = 0.0
    private var weeklyGoal: Double = 0.0
    private var monthlyGoal: Double = 0.0
    private var annualGoal: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.fragment_edit_team_goals_bottom_sheet, container, false)

        val teamsRepository = TeamsRepository(AppDatabase.getDatabase(requireContext()).teamsDao())
        val membersRepository =
            MembersRepository(AppDatabase.getDatabase(requireContext()).membersDao())
        val invitesRepository = InvitesRepository(AppDatabase.getDatabase(requireContext()).invitesDao())

        teamsViewModel = ViewModelProvider(
            requireActivity(),
            TeamsViewModelFactory(RetrofitClient.teamsService, teamsRepository, RetrofitClient.invitesService, membersRepository, invitesRepository)
        )[TeamsViewModel::class.java]

        btnClose = view.findViewById(R.id.btnClose)
        txtDailyGoal = view.findViewById(R.id.txtDailyGoal)
        txtWeeklyGoal = view.findViewById(R.id.txtWeeklyGoal)
        txtMonthlyGoal = view.findViewById(R.id.txtMonthlyGoal)
        txtAnnualGoal = view.findViewById(R.id.txtAnnualGoal)

        btnEditAll = view.findViewById(R.id.btnEditAll)
        btnInfo = view.findViewById(R.id.btnInfo)
        btnConfirm = view.findViewById(R.id.btnConfirm)

        progressBarConfirm = view.findViewById(R.id.progressBarConfirm)
        btnConfirmContainer = view.findViewById(R.id.btnConfirmContainer)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomSheet =
            dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.peekHeight = 2000
        }

        arguments?.let { args ->
            teamHandle = args.getString("TEAM_HANDLE") ?: ""
            teamId = args.getInt("TEAM_ID")
            userId = args.getInt("USER_ID")
            accessToken = args.getString("ACCESS_TOKEN") ?: ""
            dailyGoal = args.getDouble("dailyGoal")
            weeklyGoal = args.getDouble("weeklyGoal")
            monthlyGoal = args.getDouble("monthlyGoal")
            annualGoal = args.getDouble("annualGoal")

            txtDailyGoal.text = dailyGoal.toInt().toString()
            txtWeeklyGoal.text = weeklyGoal.toInt().toString()
            txtMonthlyGoal.text = monthlyGoal.toInt().toString()
            txtAnnualGoal.text = annualGoal.toInt().toString()
        }

        btnClose.setOnClickListener { dismiss() }

        btnConfirm.setOnClickListener {
            dailyGoal = txtDailyGoal.text.toString().toDouble()
            weeklyGoal = txtWeeklyGoal.text.toString().toDouble()
            monthlyGoal = txtMonthlyGoal.text.toString().toDouble()
            annualGoal = txtAnnualGoal.text.toString().toDouble()
            updateTeamGoals(accessToken, teamId, dailyGoal, weeklyGoal, monthlyGoal, annualGoal)
        }

        btnInfo.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Informação sobre as Unidades de Medida")
                .setMessage("As unidades aqui inseridas estão por padrão em Toneladas, se desejar as altere nas configurações posteriormente")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        btnEditAll.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_edit_all, null)
            val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroup)
            val txtValue = dialogView.findViewById<EditText>(R.id.txtValue)

            AlertDialog.Builder(requireContext())
                .setTitle("Edite todas as metas")
                .setView(dialogView)
                .setPositiveButton("Confirmar") { dialog, _ ->
                    val selectedId = radioGroup.checkedRadioButtonId
                    val selectedFrequency = when (selectedId) {
                        R.id.radioDaily -> "Daily"
                        R.id.radioWeekly -> "Weekly"
                        R.id.radioMonthly -> "Monthly"
                        R.id.radioAnnual -> "Annual"
                        else -> "Not Selected"
                    }

                    val inputValueString = txtValue.text.toString()

                    if (selectedFrequency == "Not Selected") {
                        showToast("Selecione o Parâmetro")
                    } else if (inputValueString.isEmpty()) {
                        showToast("Preencha o campo de meta")
                    } else {
                        try {
                            val inputValue = inputValueString.toInt()
                            when (selectedFrequency) {
                                "Daily" -> {
                                    txtDailyGoal.text = inputValue.toString()
                                    txtWeeklyGoal.text = (inputValue * 7).toString()
                                    txtMonthlyGoal.text = (inputValue * 30).toString()
                                    txtAnnualGoal.text = (inputValue * 365).toString()
                                }

                                "Weekly" -> {
                                    txtDailyGoal.text = (inputValue / 7).toString()
                                    txtWeeklyGoal.text = inputValue.toString()
                                    txtMonthlyGoal.text = (inputValue * 4).toString()
                                    txtAnnualGoal.text = (inputValue * 52).toString()
                                }

                                "Monthly" -> {
                                    txtDailyGoal.text = (inputValue / 30).toString()
                                    txtWeeklyGoal.text = (inputValue / 4).toString()
                                    txtMonthlyGoal.text = inputValue.toString()
                                    txtAnnualGoal.text = (inputValue * 12).toString()
                                }

                                "Annual" -> {
                                    txtDailyGoal.text = (inputValue / 365).toString()
                                    txtWeeklyGoal.text = (inputValue / 52).toString()
                                    txtMonthlyGoal.text = (inputValue / 12).toString()
                                    txtAnnualGoal.text = inputValue.toString()
                                }
                            }
                            showToast("Metas definidas!")
                        } catch (e: NumberFormatException) {
                            showToast("Valor inválido. Insira um número.")
                        }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
                .show()
        }


    }

    private fun updateTeamGoals(
        accessToken: String,
        teamId: Int,
        dailyGoal: Double,
        weeklyGoal: Double,
        monthlyGoal: Double,
        annualGoal: Double
    ) {
        showButtonLoading(true, btnConfirm, progressBarConfirm)
        teamsViewModel.updateTeamGoals(
            accessToken,
            teamId,
            dailyGoal,
            weeklyGoal,
            monthlyGoal,
            annualGoal
        ) {
            teamsViewModel.getTeamById(teamId)
        }
        teamsViewModel.updateResponse.observe(this) { result ->
            result.onSuccess {
                showToast("Metas editadas com sucesso!")
                showButtonLoading(false, btnConfirm, progressBarConfirm)
                dismiss()
            }
            result.onFailure {
                showToast("ERRO!")
                showButtonLoading(false, btnConfirm, progressBarConfirm)
            }
        }
    }

    private fun showButtonLoading(isLoading: Boolean, button: Button, progressBar: ProgressBar) {
        if (isLoading) {
            button.text = ""
            progressBar.visibility = View.VISIBLE
            button.isClickable = false
        } else {
            button.text = when (button.id) {
                R.id.btnAction -> "CONTINUAR"
                R.id.btnSignUp -> "CADASTRAR"
                else -> button.text.toString()
            }
            progressBar.visibility = View.GONE
            button.isClickable = true
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}