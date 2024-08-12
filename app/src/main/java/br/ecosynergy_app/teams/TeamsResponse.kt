package br.ecosynergy_app.teams

data class AllTeamsResponse(
    val _embedded: Embedded,
    val _links: PaginationLinks,
    val page: PageDetails
)

data class Embedded(
    val teamVOList: List<TeamsResponse>
)

data class TeamsResponse(
    val id: String,
    val handle: String,
    val name: String,
    val description: String,
    val timeZone: String,
    val createdAt: String,
    val updatedAt: String,
    val members: List<Member>,
    val _links: TeamLinks
)

data class Member(
    val id: Int?,
    val role: String
)

data class RoleRequest(
    val role : String?
)

data class TeamsRequest(
    val handle: String,
    val name: String,
    val description: String,
    val members: List<Member>
)

data class UpdateRequest(
    val handle: String?,
    val name: String?,
    val description: String?,
    val timeZone: String?
)

data class TeamLinks(
    val self: Href
)

data class PaginationLinks(
    val self: Href,
    val first: Href? = null,
    val next: Href? = null,
    val last: Href? = null
)

data class Href(
    val href: String
)

data class PageDetails(
    val size: Int,
    val totalElements: Int,
    val totalPages: Int,
    val number: Int
)
