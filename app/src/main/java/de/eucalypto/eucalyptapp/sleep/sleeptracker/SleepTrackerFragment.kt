/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.eucalypto.eucalyptapp.sleep.sleeptracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import de.eucalypto.eucalyptapp.databinding.FragmentSleepTrackerBinding
import de.eucalypto.eucalyptapp.sleep.database.SleepDatabase

/**
 * A fragment with buttons to record start and end times for sleep, which are saved in
 * a database. Cumulative data is displayed in a simple scrollable TextView.
 * (Because we have not learned about RecyclerView yet.)
 */
class SleepTrackerFragment : Fragment() {

    /**
     * Called when the Fragment is ready to display content to the screen.
     *
     * This function uses DataBindingUtil to inflate R.layout.fragment_sleep_quality.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val viewModel = getViewModel()

        // Get a reference to the binding object and inflate the fragment views.
        val binding = FragmentSleepTrackerBinding.inflate(inflater, container, false)
        binding.sleepTrackerViewModel = viewModel
        binding.lifecycleOwner = this


        viewModel.navigateToSleepQuality.observe(viewLifecycleOwner) { night ->
            if (night == null) return@observe
            findNavController().navigate(
                SleepTrackerFragmentDirections
                    .actionSleepShowQualityInput(night.nightId)
            )
            viewModel.completeNavigation()
        }

        return binding.root
    }

    private fun getViewModel(): SleepTrackerViewModel {
        val application = this.requireActivity().application
        val dataSource = SleepDatabase.getInstance(application).sleepDatabaseDao
        val viewModelFactory = SleepTrackerViewModelFactory(dataSource, application)
        val viewModel =
            ViewModelProvider(this, viewModelFactory)
                .get(SleepTrackerViewModel::class.java)

        return viewModel
    }
}
