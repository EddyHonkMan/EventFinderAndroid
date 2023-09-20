package com.example.eventfinder

import org.json.JSONObject

@kotlinx.serialization.Serializable
data class EventData(
    val eventName: String = "testSomeLonglonglonglonglongEventNametestSomeLonglonglonglonglongEventName",
    val venueName: String = "testSomelonglonglonglonglongVenueNametestSomeLonglonglonglonglongEventName",
    val broadGenre: String = "testGenre",
    val date: String = "03/20/2023",
    val time: String = "7:30 PM",
    val iconURL: String = "https://s1.ticketm.net/dam/c/fbc/b293c0ad-c904-4215-bc59-8d7f2414dfbc_106141_RETINA_PORTRAIT_3_2.jpg",
    val eventId: String = "rZ7HnEZ1AK7NvA",
    val ticketURL: String = "",
    val priceRange: String = "",
    val ticketStatus: String = "",
    val genres: String = "",
    val seatMapURL: String = "",
    val musicRelated: Boolean = false,
    val attractions: String = "",
    val musicRelatedArtists: List<String> = listOf(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EventData) return false
        return toString() == other.toString()
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }

    override fun toString(): String {
        return listOf(eventId, eventName, date, time).joinToString(",")
    }

    companion object {

        fun fromJson(jsonObject: JSONObject?): EventData? {
            if (jsonObject == null || jsonObject.length() == 0) return null
            val eventName = jsonObject.optString("name") ?: ""
            val venueName =
                jsonObject.optJSONObject("_embedded")?.optJSONArray("venues")?.optJSONObject(0)
                    ?.optString("name") ?: ""
            var genre = jsonObject.optJSONArray("classifications")?.optJSONObject(0)
                ?.optJSONObject("segment")?.optString("name") ?: ""
            genre = (if (genre != "Undefined") genre else "")
            var date =
                jsonObject.optJSONObject("dates")?.optJSONObject("start")?.optString("localDate")
                    ?: ""
            if (date.isNotEmpty()) {
                val tmpDate = Utilities.inputDateFormatter.parse(date)
                date = Utilities.outputDateFormatter.format(tmpDate)
            }
            var time =
                jsonObject.optJSONObject("dates")?.optJSONObject("start")?.optString("localTime")
                    ?: ""
            if (time.isNotEmpty()) {
                val tmpTime = Utilities.inputTimeFormatter.parse(time)
                time = Utilities.outputTimeFormatter.format(tmpTime ?: "")
            }
            val iconURL =
                jsonObject.optJSONArray("images")?.optJSONObject(0)?.optString("url") ?: ""
            val eventId = jsonObject.optString("id") ?: ""
            val ticketURL = jsonObject.optString("url") ?: ""

            val priceJsonObject = jsonObject.optJSONArray("priceRanges")?.optJSONObject(0)
            var priceRange = ""
            if (priceJsonObject != null) {
                val priceMin = priceJsonObject.optString("min") ?: ""
                val priceMax = priceJsonObject.optString("max") ?: ""
                val priceCurrency = priceJsonObject.optString("currency") ?: ""
                if (priceMin != "" && priceMax != "") {
                    priceRange =
                        (if (priceMin == priceMax) priceMin else "$priceMin - $priceMax") + (if (priceCurrency != "") " ($priceCurrency)" else "")
                }
            }

            val ticketStatusCode =
                jsonObject.optJSONObject("dates")?.optJSONObject("status")?.optString("code") ?: ""
            val ticketStatus = (if (ticketStatusCode == "onsale") "On Sale" else {
                if (ticketStatusCode == "offsale") "Off Sale" else ticketStatusCode.replaceFirstChar { it.uppercase() }
            })

            val genres: String = run {
                val genreJsonObject = jsonObject.optJSONArray("classifications")?.optJSONObject(0)
                if (genreJsonObject == null) {
                    ""
                } else {
                    val knownClassifications =
                        listOf("segment", "genre", "subGenre", "type", "subType")
                    val res = mutableListOf<String>()
                    for (knowClassification in knownClassifications) {
                        val genre =
                            genreJsonObject.optJSONObject(knowClassification)?.optString("name")
                        if (genre != null && genre.isNotEmpty() && genre != "Undefined") {
                            res += genre
                        }
                    }
                    res.joinToString(separator = " | ")
                }
            }

            val seatMapURL = jsonObject.optJSONObject("seatmap")?.optString("staticUrl") ?: ""

            val musicRelated = ((jsonObject.optJSONArray("classifications")?.optJSONObject(0)
                ?.optJSONObject("segment")?.optString("name") ?: "") == "Music")

            val attractionsList = mutableListOf<String>()
            jsonObject.optJSONObject("_embedded")?.optJSONArray("attractions")?.let {
                for (i in 0 until it.length()) {
                    it.getJSONObject(i).optString("name").let { attractionsList.add(it) }
                }
            }
            val attractions =
                attractionsList.filter { it.isNotEmpty() }.joinToString(separator = " | ")

            val musicRelatedArtists: MutableList<String> = mutableListOf()
            if (musicRelated) {
                val attractionsJsonArray =
                    jsonObject.optJSONObject("_embedded")?.optJSONArray("attractions")
                if (attractionsJsonArray != null) {
                    for (i in 0 until attractionsJsonArray.length()) {
                        val attractionJsonObject = attractionsJsonArray.getJSONObject(i)
                        val artistGenre =
                            attractionJsonObject.optJSONArray("classifications")?.optJSONObject(0)
                                ?.optJSONObject("segment")?.optString("name") ?: ""
                        if (artistGenre == "Music") {
                            musicRelatedArtists += attractionJsonObject.optString("name") ?: ""
                        }
                    }
                }
            }

            return EventData(
                eventName,
                venueName,
                genre,
                date,
                time,
                iconURL,
                eventId,
                ticketURL,
                priceRange,
                ticketStatus,
                genres,
                seatMapURL,
                musicRelated,
                attractions,
                musicRelatedArtists
            )
        }

        fun listFromJson(jsonObject: JSONObject?): List<EventData> {
            val events = jsonObject?.optJSONObject("_embedded")?.optJSONArray("events")
            if (events == null || events.length() == 0) return listOf()
            val res: MutableList<EventData?> = mutableListOf()
            for (i in 0 until events.length()) {
                res.add(fromJson(events.optJSONObject(i)))
            }
            return res.filterNotNull()
        }
    }

}
