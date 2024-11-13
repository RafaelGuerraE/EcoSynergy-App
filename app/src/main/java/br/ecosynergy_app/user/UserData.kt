package br.ecosynergy_app.user

data class UserResponse(
    val id: Int,
    val username: String,
    val fullName: String,
    val email: String,
    val gender: String,
    val nationality: String,
    val createdAt: String,
    val accountNonExpired: Boolean,
    val accountNonLocked : Boolean,
    val credentialsNonExpired: Boolean,
    val enabled: Boolean
)

data class ForgotRequest(
    val email: String,
    val password: String
)

data class UpdatePreferencesRequest(
    val userId: Int,
    val fireDetection: Boolean,
    val inviteStatus: Boolean,
    val inviteReceived: Boolean,
    val teamGoalReached: Boolean,
    val platform: String
)

data class PreferencesResponse(
    val id: Int,
    val userId:Int,
    val platform: String,
    val fireDetection: Boolean,
    val inviteStatus: Boolean,
    val inviteReceived: Boolean,
    val teamGoalReached: Boolean,
    val createdAt: String,
    val updatedAt: String
)

data class UpdateRequest(
    val username: String,
    val fullName: String,
    val email: String,
    val gender: String,
    val nationality: String
)

data class PasswordRequest(
    val username: String,
    val password: String
)