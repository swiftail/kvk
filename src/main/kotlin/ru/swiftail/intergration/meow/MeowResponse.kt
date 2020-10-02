package ru.swiftail.intergration.meow

data class MeowResponse(
    val status: Int,
    val data: MeowData
)

@Suppress("EnumEntryName")
enum class MeowType {
    jpg, mp4, png, gifv
}

data class MeowData(
    val type: MeowType,
    val file: String,
    val size: Int,
    val url: String
)
