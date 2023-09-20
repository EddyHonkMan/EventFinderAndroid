package com.example.eventfinder

import org.json.JSONArray
import org.json.JSONObject
import java.lang.Integer.min

data class MusicRelatedArtistData(
    val artistName: String = "",
    val artistId: String = "",
    val numOfFollowers: String = "",
    val spotifyLink: String = "",
    val popularity: String = "",
    val artistIconURL: String = "",
    val albumURLs: MutableList<String> = mutableListOf()
) {
    companion object{
        fun basicDataFromJson(artistJsonObject: JSONObject?): MusicRelatedArtistData? {
            if (artistJsonObject == null || artistJsonObject.length() == 0) return null
            val artistName = artistJsonObject.optString("name") ?: ""
            val artistId = artistJsonObject.optString("id") ?: ""
            val numOfFollowers = artistJsonObject.optJSONObject("followers")?.optString("total") ?: ""
            val spotifyLink = artistJsonObject.optJSONObject("external_urls")?.optString("spotify") ?: ""
            val popularity = artistJsonObject.optString("popularity") ?: ""
            val artistIconURL = artistJsonObject.optJSONArray("images")?.optJSONObject(0)?.optString("url") ?: ""
            return MusicRelatedArtistData(artistName, artistId, numOfFollowers, spotifyLink, popularity, artistIconURL)
        }

        fun populateAlbumsDataFromJson(albumsJsonArray: JSONArray?, target: MusicRelatedArtistData) {
            if (albumsJsonArray == null || albumsJsonArray.length() == 0) {
                return
            }
            val len = min(albumsJsonArray.length(), 3)
            for (i in 0 until len) {
                val albumURL = albumsJsonArray.optJSONObject(i)?.optJSONArray("images")?.optJSONObject(0)?.optString("url") ?: ""
                if (albumURL.isNotEmpty()) {
                    target.albumURLs += albumURL
                }
            }
        }
    }
}
