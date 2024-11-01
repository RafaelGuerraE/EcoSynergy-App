package br.ecosynergy_app.readings

import com.google.gson.annotations.SerializedName

data class MQ7ReadingsResponse(
    @SerializedName("_embedded")
    val embedded: MQ7ReadingEmbedded?,

    @SerializedName("_links")
    val links: PaginationLinks,

    val page: PageDetails
)

data class MQ135ReadingsResponse(
    @SerializedName("_embedded")
    val embedded: MQ135ReadingEmbedded?,

    @SerializedName("_links")
    val links: PaginationLinks,

    val page: PageDetails
)

data class FireReadingsResponse(
    @SerializedName("_embedded")
    val embedded: FireReadingEmbedded?,

    @SerializedName("_links")
    val links: PaginationLinks,

    val page: PageDetails
)

data class MQ7ReadingEmbedded(
    @SerializedName("mQ7ReadingVOList")
    val readings: List<MQ7ReadingVO>
)

data class MQ135ReadingEmbedded(
    @SerializedName("mQ135ReadingVOList")
    val readings: List<MQ135ReadingVO>
)

data class FireReadingEmbedded(
    @SerializedName("fireReadingVOList")
    val readings: List<FireReadingVO>
)

data class MQ7ReadingVO(
    val id: Int,
    val teamHandle: String,
    val value: Double,
    val timestamp: String,

    @SerializedName("_links")
    val links: ReadingLinks
)

data class MQ135ReadingVO(
    val id: Int,
    val teamHandle: String,
    val value: Double,
    val timestamp: String,

    @SerializedName("_links")
    val links: ReadingLinks
)

data class FireReadingVO(
    val id: Int,
    val fire: Boolean,
    val teamHandle: String,
    val timestamp: String,

    @SerializedName("_links")
    val links: ReadingLinks
)

data class ReadingLinks(
    val self: HrefLink
)

data class PaginationLinks(
    val first: HrefLink,
    val self: HrefLink,
    val next: HrefLink?,
    val last: HrefLink
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
