package com.example.android.politicalpreparedness.election

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.SingleLiveEvent
import com.example.android.politicalpreparedness.database.ElectionDatabase.Companion.getInstance
import com.example.android.politicalpreparedness.database.Repository
import com.example.android.politicalpreparedness.database.Result
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.jsonadapter.ElectionAdapter
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.launch
import retrofit2.await

class VoterInfoViewModel(private val application: Application) : ViewModel() {

    val showLoading: SingleLiveEvent<Boolean> = SingleLiveEvent()

    private val dao = getInstance(application).electionDao

    private val repository = Repository(dao)

    private val _voterInfo = MutableLiveData<VoterInfoResponse>()
    val voterInfo: LiveData<VoterInfoResponse>
        get() = _voterInfo

    private val _address = MutableLiveData<String>()
    val address: LiveData<String>
        get() = _address

    var election: Election? = null

    private val _saveButtonName = MutableLiveData<String>()
    val saveButtonName: LiveData<String>
        get() = _saveButtonName

    private val _electionData = MutableLiveData<Election?>()
    val electionData: LiveData<Election?>
        get() = _electionData

    fun getVoterInfo(id: String, division: String) {
        val divisionAdapter = ElectionAdapter()
        showLoading.value = true
        viewModelScope.launch {
            try {
                val result =
                    CivicsApi.retrofitService.getVoterInfo(division, id.toLong(), true, true)
                        .await()
                showLoading.postValue(false)
                election = Election(
                    id = result.election.id,
                    name = result.election.name,
                    electionDay = result.election.electionDay,
                    ocdDivisionId = result.election.ocdDivisionId,
                    division = divisionAdapter.divisionFromJson(result.election.ocdDivisionId)
                )
                _voterInfo.value = result
                if (result.state.isNullOrEmpty()) {
                    _address.value = ""
                } else {
                    _address.value =
                        result.state.first().electionAdministrationBody.physicalAddress?.line1 ?: ""
                }
                Log.e("ELECTIONS SUCCESS", "${_voterInfo.value}")
            } catch (e: Exception) {
                Log.e("ELECTIONS ERROR", "Failure: ${e.message}")
            }
        }
    }

    fun insertElectionToDatabase() {
        viewModelScope.launch {
            if (election != null) {
                repository.insertElection(election!!)
            }
        }
    }

    fun getElectionById(id: String) {
        viewModelScope.launch {
            when (val result = repository.getElectionById(id)) {
                is Result.Success<Election> -> {
                    val electionData = result.data as Election
                    if (electionData != null) {
                        _electionData.value = electionData
                        _saveButtonName.value = "UNFOLLOW ELECTION"
                    } else {
                        _saveButtonName.value = "FOLLOW ELECTION"
                    }
                }

                is Result.Error -> {
                    Log.e("Database Error", "${result.message}")
                    _saveButtonName.value = "FOLLOW ELECTION"
                }
            }
        }
    }

    fun deleteElectionById(id: String) {
        viewModelScope.launch {
            repository.deleteElectionById(id)
        }
    }
}