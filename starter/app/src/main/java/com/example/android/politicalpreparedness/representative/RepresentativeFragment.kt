package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import java.util.Locale

class RepresentativeFragment : Fragment() {

    companion object {
        //TODO: Add Constant for Location request
        private val REQUEST_LOCATION_PERMISSION = 1
    }

    //TODO: Declare ViewModel
    private val viewModel: RepresentativeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        //TODO: Establish bindings
        val binding: FragmentRepresentativeBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_representative, container, false
        )

        binding.representativeViewModel = viewModel

        binding.lifecycleOwner = this

        val representativeAdapter = RepresentativeListAdapter()

        binding.representativeRecycler.adapter = representativeAdapter

        val adapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.states, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.state.adapter = adapter

        binding.state.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                viewModel.getState(parent?.getItemAtPosition(position)?.toString() ?: "")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        viewModel.representatives.observe(viewLifecycleOwner, Observer {
            it?.let {
                representativeAdapter.submitList(it)
            }
        })

        var addressUser: Address? = null

        viewModel.addressInput.observe(viewLifecycleOwner, Observer {
            addressUser = it
        })

        binding.buttonSearch.setOnClickListener {
            if (addressUser != null) {
                viewModel.getRepresentatives(addressUser!!)
            }
        }

        binding.buttonLocation.setOnClickListener {
            hideKeyboard()
            checkLocationPermissions()
        }

        return binding.root

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //TODO: Handle location permission result to get location on permission granted
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            } else {
                Toast.makeText(
                    requireContext(), "Please enable location", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun checkLocationPermissions() {
        if (isPermissionGranted()) {
            getLocation()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun isPermissionGranted(): Boolean {
        //TODO: Check if permission is already granted and return (true = granted, false = denied/other)
        val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        return ContextCompat.checkSelfPermission(
            requireContext(), locationPermission
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        //TODO: Get location from LocationServices
        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        //TODO: The geoCodeLocation method is a helper function to change the lat/long location to a human readable street address
        if (lastKnownLocation != null) {
            geoCodeLocation(lastKnownLocation).let { address ->
                if (address != null) {
                    viewModel.getAddress(address)
                    viewModel.getRepresentatives(address)
                }
            }
        } else {
            Toast.makeText(
                requireContext(), "Please setup location", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun geoCodeLocation(location: Location): Address? {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        Log.e("address", "$address")
        return address?.map { address1 ->
            Address(
                line1 = address1.thoroughfare ?: "",
                line2 = address1.subThoroughfare ?: "",
                city = address1.locality ?: "",
                state = address1.adminArea ?: "",
                zip = address1.postalCode ?: ""
            )
        }!!.first()
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

}