package com.example.android.politicalpreparedness.election

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.SingleLiveEvent
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.database.Repository
import com.example.android.politicalpreparedness.database.Result
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.jsonadapter.ElectionAdapter
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.launch
import retrofit2.await

//TODO: Construct ViewModel and provide election datasource
class ElectionsViewModel(application: Application) : ViewModel() {

    private val dao = ElectionDatabase.getInstance(application).electionDao

    private val repository = Repository(dao)

    val showLoading: SingleLiveEvent<Boolean> = SingleLiveEvent()

    private val _upcomingElections = MutableLiveData<List<Election>>()
    val upcomingElections: LiveData<List<Election>>
        get() = _upcomingElections

    private val _savedElections = MutableLiveData<List<Election>>()
    val savedElections: LiveData<List<Election>>
        get() = _savedElections

    private val _navigateVoterInfo = MutableLiveData<Election?>()
    val navigateVoterInfo: LiveData<Election?>
        get() = _navigateVoterInfo

    init {
        getElections()
    }

    //TODO: Create val and functions to populate live data for upcoming elections from the API and saved elections from local database
    fun getElections() {
        val divisionAdapter = ElectionAdapter()
        showLoading.value = true
        viewModelScope.launch {
            try {
                val result = CivicsApi.retrofitService.getElections().await()
                Log.e("ELECTIONS SUCCESS", "$result")
                showLoading.postValue(false)

                _upcomingElections.value = result.elections.map { election ->
                    Election(
                        id = election.id,
                        name = election.name,
                        electionDay = election.electionDay,
                        ocdDivisionId = election.ocdDivisionId,
                        division = divisionAdapter.divisionFromJson(election.ocdDivisionId)
                    )
                }
            } catch (e: Exception) {
                Log.e("ELECTIONS ERROR", "Failure: ${e.message}")
            }
        }
    }

    fun getSavedElection() {
        viewModelScope.launch {
            when (val result = repository.getAllElection()) {
                is Result.Success<List<Election>> -> {
                    val elections = result.data as List<Election>
                    _savedElections.value = elections
                }

                is Result.Error -> Log.e("Database Error", "${result.message}")
            }
        }
    }

    //TODO: Create functions to navigate to saved or upcoming election voter info

    fun onElectionClicked(election: Election) {
        _navigateVoterInfo.value = election
    }

    fun onNavigated() {
        _navigateVoterInfo.value = null
    }
}