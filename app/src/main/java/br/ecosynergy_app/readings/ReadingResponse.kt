package br.ecosynergy_app.readings

import com.google.gson.annotations.SerializedName

data class MQ7ReadingsResponse(
    @SerializedName("_embedded")
    val embedded: MQ7ReadingEmbedded,

    @SerializedName("_links")
    val links: PaginationLinks,

    val page: PageDetails
)

data class MQ135ReadingsResponse(
    @SerializedName("_embedded")
    val embedded: MQ135ReadingEmbedded,

    @SerializedName("_links")
    val links: PaginationLinks,

    val page: PageDetails
)

data class FireReadingsResponse(
    @SerializedName("_embedded")
    val embedded: FireReadingEmbedded,

    @SerializedName("_links")
    val links: PaginationLinks,

    val page: PageDetails
)

data class MQ7ReadingEmbedded(
    val mQ7ReadingVOList: List<ReadingVO>
)

data class FireReadingEmbedded(
    val fireReadingVOList: List<ReadingVO>
)

data class MQ135ReadingEmbedded(
    val mQ135ReadingVOList: List<ReadingVO>
)

data class ReadingVO(
    val id: String,
    val teamHandle: String,
    val value: Double,
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
    val next: HrefLink,
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
