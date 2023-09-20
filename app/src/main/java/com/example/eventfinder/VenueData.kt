package com.example.eventfinder

import org.json.JSONObject

data class VenueData(
    val venueName: String = "",
    val address: String = "",
    val cityAndState: String = "",
    val contactInfo: String = "",
    val openHours: String = "",
    val generalRules: String = "",
    val childRules: String = "",
    val lng: String = "",
    val lat: String = "",
) {
    companion object{
        fun fromJson(jsonObject: JSONObject?): VenueData? {
            if (jsonObject == null || jsonObject.length() == 0) {
                return null
            }
            val tmp = jsonObject.optJSONObject("_embedded")?.optJSONArray("venues")?.optJSONObject(0)
                ?: return VenueData()
            val venueName = tmp.optString("name") ?: ""
            val address = tmp.optJSONObject("address")?.optString("line1") ?: ""

            val city = tmp.optJSONObject("city")?.optString("name") ?: ""
            val state = tmp.optJSONObject("state")?.optString("name") ?: ""
            val tmpList = listOf(city, state).filter { it.isNotEmpty() }
            val cityAndState = tmpList.joinToString(separator = ", ")

            val contactInfo = tmp.optJSONObject("boxOfficeInfo")?.optString("phoneNumberDetail") ?: ""
            val openHours = tmp.optJSONObject("boxOfficeInfo")?.optString("openHoursDetail") ?: ""
            val generalRules = tmp.optJSONObject("generalInfo")?.optString("generalRule") ?: ""
            val childRules = tmp.optJSONObject("generalInfo")?.optString("childRule") ?: ""
            val lng = tmp.optJSONObject("location")?.optString("longitude") ?: ""
            val lat = tmp.optJSONObject("location")?.optString("latitude") ?: ""
            return VenueData(venueName, address, cityAndState, contactInfo, openHours, generalRules, childRules, lng, lat)
        }
    }
}
