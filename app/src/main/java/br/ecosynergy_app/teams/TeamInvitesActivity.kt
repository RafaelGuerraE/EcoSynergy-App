package br.ecosynergy_app.teams

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.invites.Invites
import br.ecosynergy_app.room.invites.InvitesRepository
import br.ecosynergy_app.room.teams.MembersRepository
import br.ecosynergy_app.room.teams.TeamsRepository
import br.ecosynergy_app.teams.invites.InvitesAdapter
import br.ecosynergy_app.teams.viewmodel.TeamsViewModel
import br.ecosynergy_app.teams.viewmodel.TeamsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TeamInvitesActivity : AppCompatActivity() {

    private lateinit var teamsViewModel: TeamsViewModel


    private val teamsRepository = TeamsRepository(AppDatabase.getDatabase(this).teamsDao())
    private val membersRepository = MembersRepository(AppDatabase.getDatabase(this).membersDao())
    private val invitesRepository = InvitesRepository(AppDatabase.getDatabase(this).invitesDao())

    private lateinit var btnBack: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var invitesAdapter: InvitesAdapter
    private lateinit var txtError: TextView

    private var teamId: Int = 0
    private var accessToken: String = ""
    private var teamHandle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_invites)

        teamsViewModel = ViewModelProvider(
            this,
            TeamsViewModelFactory(RetrofitClient.teamsService, teamsRepository, RetrofitClient.invitesService, membersRepository, invitesRepository)
        )[TeamsViewModel::class.java]

        teamId = intent.getIntExtra("TEAM_ID", 0)
        accessToken = intent.getStringExtra("ACCESS_TOKEN").toString()

        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        txtError = findViewById(R.id.txtError)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        teamsViewModel.invitesList.observe(this) { invites ->
            if (invites.isEmpty()) {
                txtError.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                txtError.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE

                invitesAdapter = InvitesAdapter(invites)
                recyclerView.adapter = invitesAdapter
            }
        }
    }

    override fun onStart() {
        super.onStart()

        loadInvites()
    }

    private fun loadInvites(){
        teamsViewModel.getInvitesByTeam(teamId)
    }
}