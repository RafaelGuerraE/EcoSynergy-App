package br.ecosynergy_app.room.invites

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.ecosynergy_app.teams.invites.InviteResponse

@Dao
interface InvitesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvite(invite: Invites)

    @Update
    suspend fun updateInvite(invite: Invites)

    @Query("SELECT * FROM Invites WHERE id = :inviteId LIMIT 1")
    suspend fun getInviteById(inviteId: Int): Invites?

    @Query("SELECT * FROM invites WHERE teamId = :teamId")
    suspend fun findInvitesByTeam(teamId: Int): List<Invites>
}