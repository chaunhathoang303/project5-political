package com.example.android.politicalpreparedness.representative

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch
import retrofit2.await

class RepresentativeViewModel : ViewModel() {

    //TODO: Establish live data for representatives and address
    private val _representatives = MutableLiveData<List<Representative>>()
    val representatives: LiveData<List<Representative>>
        get() = _representatives

    private val _addressInput = MutableLiveData(
        Address(line1 = "", city = "", state = "", zip = "")
    )
    val addressInput: LiveData<Address>
        get() = _addressInput

    fun getState(state: String) {
        _addressInput.value?.state = state
    }

    fun getAddress(address: Address) {
        _addressInput.value = address
    }

    fun getRepresentatives(address: Address) {
        viewModelScope.launch {
            try {
                val (offices, officials) =
                    CivicsApi.retrofitService.getRepresentatives(address = address.toFormattedString())
                        .await()
                Log.e("ELECTIONS SUCCESS", "$offices")
                _representatives.value =
                    offices.flatMap { office -> office.getRepresentatives(officials) }
            } catch (e: Exception) {
                Log.e("ELECTIONS ERROR", "Failure: ${e.message}")
            }

        }
    }

}
