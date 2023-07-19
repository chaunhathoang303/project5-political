package com.example.android.politicalpreparedness.network.models

data class ElectionItem(
    val id: String,
    val name: String,
    val electionDay: String,
    val ocdDivisionId: String,
)