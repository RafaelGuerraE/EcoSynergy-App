package br.ecosynergy_app.invites

data class InviteRequest(
    val senderId: Int,
    val recipientId: Int,
    val teamId: Int
)

data class InviteResponse(
    val id: Int,
    val senderId: Int,
    val recipientId: Int,
    val teamId: Int,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)

data class InviteVOList(
    val inviteVOList: List<InviteResponse>
)

data class InviteLinks(
    val self: HrefLink
)

data class HrefLink(
    val href: String
)

data class PageDetails(
    val size: Int,
    val totalElements: Int,
    val totalPages: Int,
    val number: Int
)

data class InviteApiResponse(
    val _embedded: InviteVOList,
    val _links: InviteLinks,
    val page: PageDetails
)