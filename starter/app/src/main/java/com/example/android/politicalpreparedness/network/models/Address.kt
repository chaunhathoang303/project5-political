package com.example.android.politicalpreparedness.network.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Address(
    var locationName: String? = null,
    var line1: String? = null,
    var line2: String? = null,
    var city: String? = null,
    var state: String? = null,
    var zip: String? = null,
) : Parcelable {
    fun toFormattedString(): String {
        var output = line1.plus("\n")
        if (!line2.isNullOrEmpty()) output = output.plus(line2).plus("\n")
        output = output.plus("$city, $state $zip")
        return output
    }
}