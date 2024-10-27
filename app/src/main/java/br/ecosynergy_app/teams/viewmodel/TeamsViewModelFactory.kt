package br.ecosynergy_app.teams.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.room.invites.InvitesRepository
import br.ecosynergy_app.room.teams.MembersRepository
import br.ecosynergy_app.room.teams.TeamsRepository
import br.ecosynergy_app.teams.invites.InvitesService

class TeamsViewModelFactory(private val service: TeamsService,
                            private val teamsRepository: TeamsRepository,
                            private val invitesService: InvitesService,
                            private val membersRepository: MembersRepository,
                            private val invitesRepository: InvitesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeamsViewModel::class.java)) {
            return TeamsViewModel(service, teamsRepository, invitesService, membersRepository, invitesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}