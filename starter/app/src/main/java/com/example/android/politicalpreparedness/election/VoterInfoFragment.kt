package com.example.android.politicalpreparedness.election

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.launch

class VoterInfoFragment : Fragment() {


    private val viewModel: VoterInfoViewModel by lazy {
        val application = requireNotNull(this.activity).application
        val viewModelFactory = VoterInfoViewModelFactory(application)
        ViewModelProvider(this, viewModelFactory)[VoterInfoViewModel::class.java]
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    )
            : View? {

        val binding: FragmentVoterInfoBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_voter_info, container, false
        )

        val id = VoterInfoFragmentArgs.fromBundle(requireArguments()).argElectionId

        val division = VoterInfoFragmentArgs.fromBundle(requireArguments()).argDivision

        binding.voterInfoViewModel = viewModel

        viewModel.getVoterInfo(id, division.id)

        viewModel.getElectionById(id)

        var votingUrl = ""

        var ballotUrl = ""

        var electionDatabase: Election? = null

        binding.lifecycleOwner = this

        viewModel.voterInfo.observe(viewLifecycleOwner, Observer { voterInfo ->
            voterInfo.let {
                if (!voterInfo.state.isNullOrEmpty()) {
                    votingUrl =
                        voterInfo.state.first().electionAdministrationBody.votingLocationFinderUrl
                            ?: ""
                    ballotUrl = voterInfo.state.first().electionAdministrationBody.ballotInfoUrl
                        ?: ""

                } else {
                    binding.addressGroup.visibility = View.GONE
                }

            }
        })

        binding.stateLocations.setOnClickListener {
            if (!votingUrl.isNullOrEmpty()) {
                loadUrlIntent(votingUrl)
            } else {
                null
            }
        }

        binding.stateBallot.setOnClickListener {
            if (!ballotUrl.isNullOrEmpty()) {
                loadUrlIntent(ballotUrl)
            } else {
                null
            }
        }

        viewModel.electionData.observe(viewLifecycleOwner, Observer { electionData ->
            electionDatabase = electionData
        })

        binding.saveButton.setOnClickListener {
            if (electionDatabase != null) {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.deleteElectionById(id)
                    Navigation.findNavController(requireView()).popBackStack()
                }

            } else {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.insertElectionToDatabase()
                    Navigation.findNavController(requireView()).popBackStack()
                }
            }
        }

        return binding.root
    }

    private fun loadUrlIntent(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}